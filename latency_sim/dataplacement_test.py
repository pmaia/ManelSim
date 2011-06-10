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
import unittest
from dataplacement import CoLocatedWithSecondaryRandomPlacement,\
	CoLocatedWithSecondariesLoadBalance
from device import Device
from random import random

class random_stub:
	
	def set_expected_sample(self, expected_sample_result):
		self.expected_sample_result = expected_sample_result
	
	def sample(self, population, k):
		return self.expected_sample_result 
	
class TestCoLocatedWithSecondariesLoadBalance(unittest.TestCase):

	def test_client_is_element_of_devices_sequence_error(self):
		pass
#		devices = [Device("ds_name1", 100), Device("ds_name2", 100)]
#		co_balance = CoLocatedWithSecondariesLoadBalance()	
#		self.assertRaises(ValueError, co_balance.createfile, 3, devices, devices[0], random)
	
	def test_replication_level_greater_than_num_devices_error(self):
		pass
#		devices = [Device("ds_name1", 100), Device("ds_name2", 100)]
#		client_device = Device("ds_client", 100)		
#		co_balance = CoLocatedWithSecondariesLoadBalance()
#		self.assertRaises(ValueError, co_balance.createfile, len(devices) + 1 + 1, devices, client_device, random)
	
	def test_createfile(self):
		#it should check the space ?
		pass
#		devices = [Device("ds_name1", 100), Device("ds_name2", 10), Device("ds_name3", 50)]
#		client_device = Device("ds_client", 100)
#		co_balance = CoLocatedWithSecondariesLoadBalance()
#		replication_level = 3
#		
#		choosen_devices = co_balance.createfile(replication_level, devices, client_device, None)
#		self.assertEquals(len(choosen_devices), replication_level)
#		self.assertTrue(client_device in choosen_devices)
#		self.assertEquals(choosen_devices[0], client_device)
#		
#		self.assertTrue(devices[0] in choosen_devices)
#		self.assertEquals(devices[0], choosen_devices[1])
#				
#		self.assertTrue(devices[2] in choosen_devices)
#		self.assertEquals(devices[2], choosen_devices[2])
			
class TestCoLocated_random(unittest.TestCase):

	def test_client_is_element_of_devices_sequence_error(self):
		devices = [Device(1), Device(2)]
		co_random = CoLocatedWithSecondaryRandomPlacement()	
		self.assertRaises(ValueError, co_random.createfile, 3, devices, devices[0], random)
	
	def test_replication_level_greater_than_num_devices_error(self):
		devices = [Device(1), Device(2)]
		client_device = Device(3)		
		co_random = CoLocatedWithSecondaryRandomPlacement()
		self.assertRaises(ValueError, co_random.createfile, len(devices) + 1 + 1, devices, client_device, random)
	
	def test_createfile(self):
		#it should check the space ?
		devices = [Device(1), Device(2)]
		client_device = Device(3)		
		co_random = CoLocatedWithSecondaryRandomPlacement()
		replication_level = 2
		
		randomstub = random_stub()
		randomstub.set_expected_sample([devices[1]])
		
		choosen_devices = co_random.createfile(replication_level, devices, client_device, randomstub)
		self.assertEquals(len(choosen_devices), replication_level)
		self.assertTrue(client_device in choosen_devices)
		self.assertEquals(client_device, choosen_devices[0])#primary first
		self.assertTrue(devices[1] in choosen_devices)
		
if __name__ == '__main__':
	unittest.main() 