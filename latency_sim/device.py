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
from SimPy.Globals import now
from util import LRU
import experiment
import SimPy

class File:
	
	def __init__(self, filename, first_host_device, size):
		self.size = size
		self.filename = filename
		self.first_host_device = first_host_device
		
	def __print__(self):
		return "\t".join(self.filename, self.first_host_device, self.size)

class Device():

	def __init__(self, ds_id):
		self.bus = Resource(1)
		self.storage_device = StorageDevice()
		self.ds_id = ds_id		
		self.fileset = {}
		self.cache = Cache(4096, 4 * 1024)#16 Mbytes
		SimPy.Simulation.activate(self.storage_device, self.storage_device.Run())

	def __eq__(self, other):
		return self.ds_id == other.ds_id

	def __ne__(self, other):
		return not self.__eq__(other)

	def __contains__(self, filename):
		return filename in self.fileset

	def createfile(self, filename, filesize):
		self.__createfile__(filename, filesize, experiment.god_client)

	def __createfile__(self, filename, filesize, client):
		if filename in self: raise FileAlreadyExistError(filename)
		if filesize < 0: raise ValueError(filename, filesize)
		self.fileset[filename] = File(filename, self.ds_id, 0)#creating file entry
		self.__write__(filename, 0, filesize, client)

	def delete(self, filename):
		if not filename in self: raise NoSuchFileError(filename)
		self.fileset.pop(filename)

	def copy2(self, filename, dst_device, client):
		filesize = self.filesize(filename)
		self.__read__(filename, 0, filesize, None)
		if not filename in dst_device:
			dst_device.__createfile__(filename, filesize, client)

	def read(self, filename, offset, length):
		self.__read__(filename, offset, length, experiment.god_client)

	def __read__(self, filename, offset, length, client):

		if length < 0: raise ValueError("Length must be no negative: " + str(length))
		if offset < 0: raise ValueError("Offset must be no negative: " + str(offset))
		if not filename in self : raise NoSuchFileError(filename)

		if (offset + length) > self.filesize(filename):
			if not experiment.ignore_IOB_on_read:
				raise ValueError("\t".join(["Index out of bounds. size:", str(self.filesize(filename)),\
									"offset:", str(offset), "length:", str(length)]))
			else:
				file = self.fileset[filename]
				file.size = offset + length

		begin_i = self.cache.index_bitmap(offset)
		end_i = self.cache.index_bitmap(offset + length)

		data2read_from_disk = 0
		data2read_from_cache = 0

		for index in range(begin_i, end_i):
			if not self.cache.contains(filename, index):
				data2read_from_disk = data2read_from_disk + self.cache.block_size 
			else:
				data2read_from_cache = data2read_from_cache + self.cache.block_size
			self.cache.insert(filename, index)#There is no need for a read on cache. A Insert is enough to modify the order

		last_chunk_size =  offset + length - self.cache.offset(end_i) 
		if not self.cache.contains(filename, end_i): #last chunk
			data2read_from_disk = data2read_from_disk + last_chunk_size 
		else:
			data2read_from_cache = data2read_from_cache + last_chunk_size
		self.cache.insert(filename, end_i)

		self.__hold__(self.__cache_hold_time__(data2read_from_cache))#FIXME WRONG. I GUESS THIS YIELD DOES NOT WORK. NOT SO BAD
		self.storage_device.addJob(data2read_from_disk, client)#FIXME WRONG. I GUESS THIS METHOD SHOULD USE data2read_from_disk
		reactivate(self.storage_device)

		return length

	def write(self, filename, offset, length):
		self.__write__(filename, offset, length, experiment.god_client)

	def __write__(self, filename, offset, length, client):
		
		#FIXME wrong, I think this method must change the cache state

		if length < 0: raise ValueError("Length must be no negative: " + str(length))
		if offset < 0: raise ValueError("Offset must be no negative: " + str(offset))
		if not filename in self : raise NoSuchFileError(filename)
		file = self.fileset[filename]

		total_sent2device = 0
		if offset > file.size: # writing beyond the file size, filling the gap
			total_sent2device = self.__write2device__(filename, file.size, offset - file.size)

		written_bytes = self.__write2device__(filename, offset, length)
		total_sent2device = total_sent2device + written_bytes
		self.storage_device.addJob(total_sent2device, client)
		reactivate(self.storage_device)# IMPORTANT. Only one call to reactive. Do not iterate on this call

		return written_bytes

	def __write2device__(self, filename, offset, length):
		"""
		Only append or overwriting is allowed. ( offset cannot be greater than filesize + 1 )  
		"""
		self.cache.write(filename, offset, length)
		data2write = length

		previous_filesize = self.filesize(filename)

		if offset < previous_filesize:#overwriting
			data2write = max(0, offset + length - previous_filesize)

		file = self.fileset[filename]
		file.size = previous_filesize + data2write
		return length

	def filesize(self, filename):
		if not filename in self : raise NoSuchFileError(filename)
		return self.fileset[filename].size

	def __hold__(self, time_millis):
		yield hold, self, time_millis
	def __cache_hold_time__(self, bytes):
		if bytes < 0 : raise ValueError()
		return bytes/experiment.mem_thro__bytes2milliSecs

class StorageDevice(Process):
	
	def __init__(self):
		SimPy.Simulation.Process.__init__(self)
		self.queue = []

	def addJob(self, length, client):
		self.queue.append((length, client))

	def Run(self):
		while 1:
			yield SimPy.Simulation.passivate, self
			while self.queue:
				(job_length, client) = self.queue.pop()
				yield hold, self, self.__disk_hold_time__(job_length)
				if client:
					reactivate(client)

	def __disk_hold_time__(self, bytes):
		if bytes < 0 : raise ValueError()
		return max(1, bytes/experiment.disk_thro_bytes2milliSecs)

class Cache():

	def __init__(self, block_size, capacity_in_blocks):
		self.block_size = block_size
		self.lru_blocks = LRU(capacity_in_blocks) #(filename, index)
	def index_bitmap(self, offset):
		return int(offset / self.block_size)
	def offset(self, index):
		return index * self.block_size
	def contains(self, filename, index):
		return (filename, index) in self.lru_blocks
	def write(self, filename, offset, length):
		for index in range(self.index_bitmap(offset), self.index_bitmap(offset + length)):
			self.insert(filename, index)
	def insert(self, filename, index):
		self.lru_blocks[(filename, index)] = 1# Value does not matter

class FileAlreadyExistError(Exception):
	def __init__(self, filename):
		self.filename = filename
	def __str__(self):
		return repr(self.value)

class NoSuchFileError(Exception):
	def __init__(self, filename):
		self.filename = filename
	def __str__(self):
		return repr("A file should be created before accessed")