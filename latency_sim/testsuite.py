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
from login_test import TestSweetHomeLoginAlgorithm, TestHomeLessLoginAlgorithm
from device_test import TestObjectCreation
from dataplacement_test import TestCoLocated_random,\
	TestCoLocatedWithSecondariesLoadBalance
#from naming_test import TestReplicationGroup, TestNaming
		
if __name__ == '__main__':
	
	sweetlogin_suite = unittest.TestLoader().loadTestsFromTestCase(TestSweetHomeLoginAlgorithm)
	homelesslogin_suite = unittest.TestLoader().loadTestsFromTestCase(TestHomeLessLoginAlgorithm)
	
#	replication_suite = unittest.TestLoader().loadTestsFromTestCase(TestReplicationGroup)
#	naming_suite = unittest.TestLoader().loadTestsFromTestCase(TestNaming)
	device_suite = unittest.TestLoader().loadTestsFromTestCase(TestObjectCreation)
	
	co_random_suite = unittest.TestLoader().loadTestsFromTestCase(TestCoLocated_random)
	co_balance_suite = unittest.TestLoader().loadTestsFromTestCase(TestCoLocatedWithSecondariesLoadBalance)

	alltests = unittest.TestSuite([sweetlogin_suite, homelesslogin_suite, 
								   device_suite, co_random_suite, co_balance_suite])
	unittest.TextTestRunner(verbosity=2).run(alltests)