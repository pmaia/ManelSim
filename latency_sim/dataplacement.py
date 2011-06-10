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

class RandomDataPlacement():

	def createfile(self, replication_level, list_nonfull_devices, client_device, random):
		return random.sample(list_nonfull_devices, replication_level)

class CoLocatedWithSecondariesLoadBalance():
	
	def __from_empty_to_full_sort__(self, sequence):
		return list(sequence)
	
	def createfile(self, replication_level, list_nonfull_devices, client_device, random):
		
		if client_device in list_nonfull_devices:
			raise ValueError("list_nonfull_devices and client_device must be disjoint")
		
		if replication_level > (len(list_nonfull_devices) + 1):
			raise ValueError("replication level is greater than the number of available devices")
		
		sorted_devices = sorted(list_nonfull_devices, key=lambda device: device.available(), reverse=True)
		choosen_devices = sorted_devices[0:(replication_level - 1)]
		choosen_devices.insert(0, client_device)
		return choosen_devices		

class CoLocatedWithSecondaryRandomPlacement():

	def createfile(self, replication_level, list_nonfull_devices, client_device, random):
				
		if client_device in list_nonfull_devices:
			raise ValueError("list_nonfull_devices and client_device must be disjoint")
		
		if replication_level > (len(list_nonfull_devices) + 1):
			raise ValueError("replication level is greater than the number of available devices")

		choosen_devices = random.sample(list_nonfull_devices, replication_level - 1)
		choosen_devices.insert(0, client_device)
		return choosen_devices