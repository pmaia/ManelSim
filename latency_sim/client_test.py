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
from device import Device
import unittest
from login import LoginStrategy, SweetHomeLoginAlgorithm, HomeLessLoginStrategy
from random import random
from experiment import experiment

from SimPy.Simulation import *
import experiment
import SimPy
from dataplacement import CoLocatedWithSecondaryRandomPlacement
from util import FileSizeDistribution
import math
from client import Client

class BootStrapStub:

	def __init__(self, operations):
		self.ops = operations
		self.pos = 0
	def next_op(self):
		op = self.ops[self.pos]
		self.pos = self.pos + 1
		return op

class FileSizeDistributionStub():
	def __init__(self, samples):
		self.samples = samples
		self.pos = 0
	def sample(self):
		sample = self.samples[self.pos]
		self.pos = self.pos + 1
		return sample

class DataPlacementStub():
	def __init__(self, devices):
		self.devices = devices
		self.pos = 0
	def createfile(self, r_level, tmp_sequence, client_device, random):
		device = self.devices[self.pos]
		self.pos = self.pos + 1
		return device
		
class TestMigration(unittest.TestCase):
		
	def test_migration(self):
		enableMigration = True
		migration_delay = 100
		communication_delay = 1
	
		SimPy.Simulation.initialize()#required by SimPy
	
		devices = [Device(1), Device(2)]
		placement_police = DataPlacementStub([ [devices[0]], [devices[1]] ])
		
		fileSizeDistribution = FileSizeDistributionStub( [1024 * 1024, 1024 * 1024])
	
		operations = []
		operations.append(("open", "filename", 0, devices[0], 0))#open/create a file on device 1
		operations.append(("close", 0, 10))#close the file
		operations.append(("open", "filename", 0, devices[1], 20))#open the file on device 2
		operations.append(("read", 0, 0, 4096, devices[1], 30))#read the file on device 2, do not migrate
		operations.append(("read", 0, 0, 4096, devices[1], 200))#read the file on device 2, migrate
		operations.append(None)#end mark
		boot = BootStrapStub(operations)
		
		client = Client(boot, 3, devices, placement_police, fileSizeDistribution, communication_delay, enableMigration, migration_delay)
		SimPy.Simulation.activate(client, client.Run())#required by SimPy
		SimPy.Simulation.simulate(1234567890123456L)
		
		#print experiment.logger
if __name__ == '__main__':
	unittest.main()