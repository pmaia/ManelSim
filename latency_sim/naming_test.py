# -*- coding: utf-8 -*-
import unittest
from naming import *
from device import *
import SimPy

class TestReplicationGroup(unittest.TestCase):
		
	def test_create_replicas_on_Init(self):
		ds1 = Device(1)# FIXME how to avoid repeated names
		ds2 = Device(2)
		filename = "filename"
#		group = ReplicationGroup(filename, 2, [ds1, ds2], 0)
#		self.assertEquals(ds1, group.primary)
#		self.assertEquals(1, len(group.secs))
#		self.assertTrue(ds2.ds_id in group.secs)

class TestNaming(unittest.TestCase):
	
	def test_partial_failureOnGroupCreation(self):#test total failure
		pass
#		SimPy.Simulation.initialize()#required by SimPy
#
#		class placement_stub():
#			def __init__(self, expected_sequence):
#				self.expected_sequence = expected_sequence
#			def createfile(self, r_level, tmp_sequence, client_device, random):
#				return self.expected_sequence
#		class FileSizeDistributionStub():
#			def __init__(self, expected_sample):
#				self.expected_sample = expected_sample
#			def sample(self):
#				return self.expected_sample
#
#		small_capacity = 100
#		big_capacity = small_capacity + 1
#		ds1 = Device(1, big_capacity)
#		ds2 = Device(2, small_capacity)
#		available_devices = []
#		available_devices.append(ds1)
#		available_devices.append(ds2)
#				
#		data_placement_strategy = placement_stub(available_devices)
#		f_size_dist = FileSizeDistributionStub(big_capacity)
#		
#		meta_server = MetadataServer(available_devices, data_placement_strategy, f_size_dist, len(available_devices))
#		
#		SimPy.Simulation.activate(ds1, ds1.Run())#required by SimPy	
#		SimPy.Simulation.activate(ds2, ds2.Run())#required by SimPy
#		SimPy.Simulation.activate(meta_server, meta_server.Run())#required by SimPy
#				
#		filename = "filename"
#		group = meta_server.open(filename, 0, ds1)
#		self.assertEquals(2, group.replication_level)
#		self.assertEquals(ds1, group.primary)
#		self.assertEquals(0, len(group.secs))
#		
#		self.assertTrue(filename in ds1)
#		self.assertFalse(filename in ds2)
#		self.assertEquals(big_capacity, ds1.filesize(filename))

#	def test_writeOnReplicationGroup(self): FIXME: MOVE IT TO CLIENT TEST
#		capacity = 100
#		ds1 = Device(1, capacity)
#		ds2 = Device(2, capacity)
#		filename = "filename"
#		filesize = 0
#		group = ReplicationGroup(filename, filesize, 2, [ds1, ds2])
#		self.assertEquals(filesize, ds1.filesize(filename))
#		increment = 1
#		group.write(0, increment, ds1)
#		self.assertEquals(filesize + increment, ds1.filesize(filename))

#	def test_promotionOnWrite(self):#no-delay promotion
#	
#		G.propagation_delay = 0
#	
#		capacity = 100
#		ds1 = Device(1, capacity)
#		ds2 = Device(2, capacity)
#		filename = "filename"
#		filesize = 0
#		group = ReplicationGroup(filename, filesize, 2, [ds1, ds2])
#		self.assertEquals(filesize, ds1.filesize(filename))
#		self.assertEquals(filesize, ds2.filesize(filename))
#		increment = 1
#		group.write(0, increment, ds2)
#		self.assertEquals(filesize, ds1.filesize(filename))
#		self.assertEquals(filesize + increment, ds2.filesize(filename))
		
#	def test_migrationOnWrite(self):
#		
#		G.migration = True
#		
#		capacity = 100
#		ds1 = Device(1, capacity)
#		ds2 = Device(2, capacity)
#		ds3 = Device(3, capacity)
#		
#		filename = "filename"
#		filesize = 0
#		group = ReplicationGroup(filename, filesize, 2, [ds1, ds2])
#		self.assertEquals(filesize, ds1.filesize(filename))
#		self.assertEquals(filesize, ds2.filesize(filename))
#		increment = 1
#		group.write(0, increment, ds3)
#		self.assertTrue(group.has_replica(ds1))
#		self.assertFalse(group.has_replica(ds2))
#		self.assertTrue(group.has_replica(ds3))
#		
#		self.assertEquals(filesize + increment, ds1.filesize(filename))		

if __name__ == '__main__':
	unittest.main()