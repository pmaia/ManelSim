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
import SimPy
import sys
from SimPy.Simulation import *
from BootStrap import *
from util import Log
try:
   import psyco
   psyco.full()
except:
   pass

def placement_police(policename):
	if policename == "random":
		return RandomDataPlacement()
	elif policename == "co-random":
		return CoLocatedWithSecondaryRandomPlacement()
	elif policename == "co-balance":
		return CoLocatedWithSecondariesLoadBalance()

god_client = None
logger = Log()

HOUR_IN_MILLIS = 1000 * 60 * 60
ignore_IOB_on_read = True
net_throbytes2milliSecs = 12 * 100 #12 Mbytes/s 100Mbits/sec 
disk_thro_bytes2milliSecs = 50 * 1000# 30 Mbytes/s
mem_thro__bytes2milliSecs = 1 * 1000000#1 GigaByte/smigration_delay = None

if __name__ == "__main__":

	print sys.argv
    
	can_promote = True
	communication_delay = 1

	tracefile = sys.argv[1]
	policename = sys.argv[2]#[random, co-random, co-balance]
	num_machines = int(sys.argv[3])
	homeless = {"true": True, "false": False}.get(sys.argv[4].lower())# homeless login [true, false]
	migration_prob = long(sys.argv[5])#migration probability [0, 1)
	migration = {"true": True, "false": False}.get(sys.argv[6].lower())#data_migration [true, false]
	propagation_delay = long(sys.argv[7]) * 1000#replication delay secs
	migration_delay = long(sys.argv[8]) * 1000
	hours_between_login = int(sys.argv[9])

	SimPy.Simulation.initialize()#required by SimPy

	last_stamp = -1
	trace = open(tracefile, 'r')

	placement_police = CoLocatedWithSecondaryRandomPlacement()

	diskSize = 1024 * 1024 * 1024 * 4#1 GiBytes
	fileSizeDistribution = FileSizeDistribution(8.46, math.sqrt(2.38), diskSize)

	devices = [Device(i) for i in range(num_machines)]

	if homeless:
		login_strategy = HomeLessLoginStrategy(migration_prob, experiment.HOUR_IN_MILLIS * hours_between_login, devices, Random())
	else:
		login_strategy = SweetHomeLoginAlgorithm(migration_prob, experiment.HOUR_IN_MILLIS * hours_between_login, devices, Random())

	boot = BootStrap(trace, login_strategy)
	client = Client(boot, 3, devices, placement_police, fileSizeDistribution, communication_delay, migration, migration_delay)
	boot.set_client(client)

	SimPy.Simulation.activate(client, client.Run())#required by SimPy

	SimPy.Simulation.simulate(1234567890123456L)	
	print experiment.logger
