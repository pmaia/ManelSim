# -*- coding: utf-8 -*-
#
#  Copyright (C) 2009 Universidade Federal de Campina Grande
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
# 
#          http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from SimPy.Simulation import *
import experiment
import SimPy
from random import Random

class Client(Process):

	def __init__(self, bootstrap, r_level, available_devices, data_placement_strategy, FileSizeDistribution, communication_delay, \
					migration, migration_delay):

		SimPy.Simulation.Process.__init__(self)
		
		self.files = {}
		self.default_rlevel = r_level
		self.devices = available_devices
		self.placement_strategy = data_placement_strategy
		self.filesize_distribution = FileSizeDistribution
		self.maxsize = 4 * 1024 * 1024 * 1024
		self.openfiles = {}#(filename:(group, current_pos)
		self.bootstrap = bootstrap
		self.migration_requests = {} # {filename:(last_location, first_stamp_on_last_location)}
		
		self.migration = migration
		self.migration_delay = migration_delay
		self.communication_delay= communication_delay
		self.can_promote = True
		
		experiment.god_client = self

	def __choose_devices__(self, r_level, devices, client_device):
		tmp_sequence = list(devices)
		tmp_sequence.remove(client_device)
		return self.placement_strategy.createfile(r_level, tmp_sequence, client_device, Random())

	def Run(self):

		while 1:

			op = self.bootstrap.next_op()

			if not op:
				SimPy.Simulation.stopSimulation()
				break

			if op[0] is "open":
				(filename, file_desc, client_device, hold_time) = op[1], op[2], op[3], op[4]

				yield SimPy.Simulation.hold, self, hold_time
				
				if not filename in self.files:
					choosen_devices = self.__choose_devices__(self.default_rlevel, self.devices, client_device)
					replicas = []
					filesize = min(self.filesize_distribution.sample(), self.maxsize)

					for device in choosen_devices:
						yield SimPy.Simulation.hold, self, self.communication_delay
						device.createfile(filename, filesize)
						yield SimPy.Simulation.passivate, self
						replicas.append(device)

					group = ReplicationGroup(filename, self.default_rlevel, replicas, now())
					self.files[filename] = group

				group = self.files[filename]
				group.current_pos(0)
				group.update_touch(now())
				self.openfiles[file_desc] = group
				self.__update_migration_stamp__(filename, self.migration_requests, client_device, now())

			elif op[0] is "read":

				(file_desc, offset, length, client_device, hold_time) = op[1], op[2], op[3], op[4], op[5]
				yield SimPy.Simulation.hold, self, hold_time

				begin = now()
				
				group = self.openfiles[file_desc]
				
				self.__update_migration_stamp__(group.filename, self.migration_requests, client_device, now())

				if self.migration:
					(last_location, first_stamp_on_last_location) = self.migration_requests[group.filename]
					if (last_location == client_device) and ( (now() - first_stamp_on_last_location) >= self.migration_delay):
						if not group.has_replica(client_device):
							group.primary.copy2(group.filename, client_device, self)
							yield SimPy.Simulation.passivate, self
							group.ack_migration(client_device)

				if self.can_promote:
					group.try_promotion(client_device)

				yield SimPy.Simulation.hold, self, self.communication_delay #emulated communication with MS

				group.primary.read(group.filename, offset + group.pos, length)
				yield SimPy.Simulation.passivate, self

				if not client_device is group.primary:
					network_delay = length/experiment.net_throbytes2milliSecs#data transfer through network
					yield SimPy.Simulation.hold, self, max(self.communication_delay, network_delay)

				end = now()
				group.update_touch(now())
				group.current_pos(offset + group.pos + length)
				self.__report_op__(group.filename, client_device, group.primary, "read", length, end - begin)

			elif op[0] is "write":

				(file_desc, offset, length, client_device, hold_time) = op[1], op[2], op[3], op[4], op[5]
				yield SimPy.Simulation.hold, self, hold_time
				begin = now()

				group = self.openfiles[file_desc]
				
				self.__update_migration_stamp__(group.filename, self.migration_requests, client_device, now())
				
				if self.migration:
					(last_location, first_stamp_on_last_location) = self.migration_requests[group.filename]
					if (last_location == client_device) and ( (now() - first_stamp_on_last_location) >= self.migration_delay):
						if not group.has_replica(client_device):
							group.primary.copy2(group.filename, client_device, self)
							yield SimPy.Simulation.passivate, self
							group.ack_migration(client_device)

				if self.can_promote:
					group.try_promotion(client_device)

				yield SimPy.Simulation.hold, self, self.communication_delay #emulated communication with MS

				#update all replicas
				group.primary.write(group.filename, offset + group.pos, length)
				yield SimPy.Simulation.passivate, self

				for secondary in group.secs.itervalues():
					secondary.write(group.filename, offset + group.pos, length)
					yield SimPy.Simulation.passivate, self

				if not client_device is group.primary:
					network_delay = length/experiment.net_throbytes2milliSecs#data transfer through network
					yield SimPy.Simulation.hold, self, max(self.communication_delay, network_delay)

				for secondary in group.secs.itervalues():
					if not client_device is secondary:
						network_delay = length/experiment.net_throbytes2milliSecs#data transfer through network
						yield SimPy.Simulation.hold, self, max(self.communication_delay, network_delay)

				end = now()
				group.update_touch(now())
				group.current_pos(offset + group.pos + length)
				self.__report_op__(group.filename, client_device, group.primary, "write", length, end - begin)

			elif op[0] is "close":

				(file_desc, hold_time) = op[1], op[2]
				yield SimPy.Simulation.hold, self, hold_time

				group = self.openfiles[file_desc]
				group.update_touch(now())
				group.current_pos(0)
				del self.openfiles[file_desc]
	
	def __update_migration_stamp__(self, filename, migration_requests, current_device, now):
		if (not filename in migration_requests) or ( not migration_requests[filename][0] is current_device):
			migration_requests[filename] = (current_device, now)	

	def __report_op__(self, filename, client_device, server_device, op_type, length, delay):
		experiment.logger.log_report(filename, op_type, length, client_device == server_device, delay, now())

class ReplicationGroup():

	def __init__(self, filename, replication_level, device_replicas, touch_stamp):
		if len(device_replicas) > replication_level:
			raise Exception("The number of replicas is greater than replication level")

		self.pos = 0
		self.touch_stamp = touch_stamp
		self.filename = filename
		self.replication_level = replication_level
		self.primary = device_replicas[0]
		self.secs = {}
		for sec in device_replicas[1:]:#take care, it is wrong if there is no sec replicas
			self.secs[sec.ds_id] = sec

	def __str__(self):
		replicas = [ str(ds.ds_id) for ds in self.secs.values()]
		return "filename\t" + self.filename + "\tprimary:\t" + str(self.primary.ds_id) + "\treplicas:\t" + str(replicas)

	def current_pos(self, pos):
		if pos < 0 : raise ValueError()
		self.pos = pos

	def __can_promote__(self, client_device):
		return (not self.primary == client_device) and self.has_replica(client_device)

	def try_promotion(self, client_device):
		if self.__can_promote__(client_device):
			self.__promote_sec__(client_device)
	def update_touch(self, stamp):
		if stamp < self.touch_stamp : raise ValueError()
		self.touch_stamp = stamp

	def has_replica(self, client_device):
		return (client_device == self.primary) or (client_device.ds_id in self.secs)

	def __promote_sec__(self, client_device):
		del self.secs[client_device.ds_id]
		self.__add_sec_replica__(self.primary)
		self.primary = client_device

	def __add_sec_replica__(self, device):
		self.secs[device.ds_id] = device
		if len(self.secs) > (self.replication_level - 1):
			raise ValueError("".join(self.filename, str(self.replication_level), self.primary, self.secs))# an assert

	def ack_migration(self, new_device):
		try:
			(device_name, device_to_remove) = self.secs.popitem()#choosing an arbitrary replica
			device_to_remove.delete(self.filename)
		except KeyError:
			pass#there is no secondary		
		self.__add_sec_replica__(new_device)
		experiment.logger.report_migration(self.filename)
