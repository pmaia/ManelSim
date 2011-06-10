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

class random_stub:
	
	def set_expected_uniform(self, expected):
		self.expected_uniform = expected
	
	def uniform(self, a, b):
		return self.expected_uniform 
	
	def set_expected_choise(self, expected_choice):
		self.expected_choice = expected_choice
		
	def choice(self, list):
		return self.expected_choice

class TestSweetHomeLoginAlgorithm(unittest.TestCase):
		
	def test_errors_on_init(self):
		
		clients = [Device(1), Device(2)]
		
		self.assertRaises(ValueError, LoginStrategy, -1, 0, clients, random)
		self.assertRaises(ValueError, LoginStrategy, 2, 0, clients, random)
		self.assertRaises(ValueError, LoginStrategy, 1, -1, clients, random)
		self.assertRaises(ValueError, LoginStrategy, 1, 1, None, random)
		self.assertRaises(ValueError, LoginStrategy, 1, 1, [], random)
		self.assertRaises(ValueError, LoginStrategy, 1, 1, clients, None)
		
	def test_sweet_Strategy_sampling(self):
		
		devices = [Device(1), Device(2), Device(3)]
		
		first_stamp = 0
		
		msec_between = 1
		randomstub = random_stub()		
		swap_prob = 0.8
		sweet = SweetHomeLoginAlgorithm(swap_prob, msec_between, devices, randomstub)
		self.assertEquals(devices[0], sweet.sample_client(first_stamp))#first pick, smaller than time to swap

		randomstub.set_expected_uniform(swap_prob + 0.1)#swap to sweet home
		second_stamp = first_stamp + msec_between
		self.assertEquals(devices[0], sweet.sample_client(second_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob - 0.1)#swap to any other device (excluding sweet machine)
		randomstub.set_expected_choise(devices[2])
		third_stamp = second_stamp + msec_between
		self.assertEquals(devices[2], sweet.sample_client(third_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob + 0.1)#swap to sweet home
		fourth_stamp = third_stamp + msec_between
		self.assertEquals(devices[0], sweet.sample_client(fourth_stamp))#delta greater than time to swap

class TestHomeLessLoginAlgorithm(unittest.TestCase):
			
	def test_homeless_Strategy_sampling(self):

		devices = [Device(1), Device(2), Device(3)]
		
		first_stamp = 0
		msec_between = 1
		
		randomstub = random_stub()		
		swap_prob = 0.8
		sweet = HomeLessLoginStrategy(swap_prob, msec_between, devices, randomstub)
		self.assertEquals(devices[0], sweet.sample_client(first_stamp))#first pick, smaller than time to swap

		randomstub.set_expected_uniform(swap_prob + 0.1)#no swap, return last sample
		second_stamp = first_stamp + msec_between
		self.assertEquals(devices[0], sweet.sample_client(second_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob + 0.1)#no swap, return last sample
		third_stamp = second_stamp + msec_between
		self.assertEquals(devices[0], sweet.sample_client(third_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob - 0.1)#swap to a random device
		randomstub.set_expected_choise(devices[2])
		fourth_stamp = third_stamp + msec_between
		self.assertEquals(devices[2], sweet.sample_client(fourth_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob - 0.1)#swap to a random device
		randomstub.set_expected_choise(devices[1])
		fifth_stamp = fourth_stamp + msec_between
		self.assertEquals(devices[1], sweet.sample_client(fifth_stamp))#delta greater than time to swap
		
		randomstub.set_expected_uniform(swap_prob + 0.1)#no swap, return last sample
		sixth_stamp = fifth_stamp + msec_between
		self.assertEquals(devices[1], sweet.sample_client(sixth_stamp))#delta greater than time to swap
		
if __name__ == '__main__':
	unittest.main()