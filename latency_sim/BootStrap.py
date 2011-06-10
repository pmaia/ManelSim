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
from dataplacement import RandomDataPlacement, \
	CoLocatedWithSecondaryRandomPlacement, CoLocatedWithSecondariesLoadBalance
from login import HomeLessLoginStrategy, SweetHomeLoginAlgorithm

from util import FileSizeDistribution

import math
from random import Random
from client import Client
import experiment
from device import *

class BootStrap():
	
	def __init__(self, trace, login_strategy):
		self.count = 0
		self.last_stamp = -1
		self.trace = trace
		self.magic_flag = False
		self.login_strategy = login_strategy

	def set_client(self, client):
		self.client = client

	def next_op(self):
		line = self.trace.readline()
		
		if not line:
			return line#None
		
		split = line.split()
		operation = split[0]			
		if operation == 'read':
			return self.__read__(split)
		elif operation == 'write':
			return self.__write__(split)
		elif operation == 'open':
			return self.__open__(split)
		elif operation == 'close':
			return self.__close__(split)
		else:
			raise Exception("tokenization problem")
	
	def __delta_and_advance__(self, time):
		if self.last_stamp == -1:
			self.first_stamp = time
		delta = time - self.last_stamp 
		self.last_stamp = time
		return delta
	
	def __choose_client_device__(self, now):
		return self.login_strategy.sample_client(now)
	
	def __close__(self, tokens):#close 960254662.240045 3
		time = self.__parse_time__(tokens[1])
		delta = self.__delta_and_advance__(time)
		return ("close", int(tokens[2]), delta)#must find the related client

	def __read__(self, tokens):#read 960254162.929275 3 4096
		#O trace do seer nao tem informacao sobre offset. alem disso, algumas operacoes
		#podem ser executadas sobre arquivos que nao estao em acordo com o offset real (o tamanho
		#dos arquivos eh arbitrado
		(time, fd, length) = self.__parse_time__(tokens[1]), int(tokens[2]), long(tokens[3])
		delta = self.__delta_and_advance__(time)
		client_device = self.__choose_client_device__(now())
		return ("read", fd, 0, length, client_device, delta)

	def __write__(self, tokens):#write 960254662.240004 3 1137
		(time, fd, length) = self.__parse_time__(tokens[1]), int(tokens[2]), long(tokens[3])
		delta = self.__delta_and_advance__(time)
		client_device = self.__choose_client_device__(now())
		return ("write", fd, 0, length, client_device, delta)

	def __open__(self, tokens):#open /usr/X11R6/lib/X11/fonts/misc/fonts.dir 960254165.543422 5	
		(filename, time, fd) = tokens[1], self.__parse_time__(tokens[2]), int(tokens[3])
		delta = self.__delta_and_advance__(time)
		client_device = self.__choose_client_device__(now())
		return ("open", filename, fd, client_device, delta)

	def __parse_time__(self, time):		
		return long(time)