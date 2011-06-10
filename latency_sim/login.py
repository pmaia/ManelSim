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
import experiment

class LoginStrategy():

	def __init__(self, swap_machine_prob, msecs_between_logins, devices, random):#injecting random to ease testing
		
		if swap_machine_prob < 0 or swap_machine_prob > 1:
			raise ValueError("swap_machine_prob interval must be [0, 1]")
		if msecs_between_logins < 0:
			raise ValueError("msecs_between_logins cannot be negative")		
		if devices is None:
			raise ValueError("devices must be assigned")
		if not len(devices) > 0:
			raise ValueError("the number of devices must be positive")
		if random is None:
			raise ValueError("the random generator must be assigned")
		
		self.swap_machine_prob = swap_machine_prob
		self.msecs_between_logins = msecs_between_logins		
		self.devices = devices
		self.last_sample = devices[0]
		self.random = random
		self.last_login_stamp = None

	def sample_client(self, now): 		
		if self.last_login_stamp is None:
			self.last_login_stamp = now 
		if ( now - self.last_login_stamp ) >= self.msecs_between_logins:			
			self.last_sample = self.__picksample__()
			experiment.logger.report_new_login(self.last_sample, now)
		self.last_login_stamp = now
		return self.last_sample

class SweetHomeLoginAlgorithm(LoginStrategy):
	
	def __init__(self, swap_machine_prob, msecs_between_logins, devices, random):
		LoginStrategy.__init__(self, swap_machine_prob, msecs_between_logins, devices, random)
		self.sweet_home = devices[0]
	
	def __picksample__(self):
		if self.random.uniform(0, 1) <= self.swap_machine_prob:
			return self.random.choice(self.devices[1:])
		else:
			return self.sweet_home

class HomeLessLoginStrategy(LoginStrategy):
	
	def __init__(self, swap_machine_prob, msecs_between_logins, devices, random):
		LoginStrategy.__init__(self, swap_machine_prob, msecs_between_logins, devices, random)		
		
	def __picksample__(self):
		if self.random.uniform(0, 1) <= self.swap_machine_prob:
			return self.random.choice(self.devices)
		else:
			return self.last_sample