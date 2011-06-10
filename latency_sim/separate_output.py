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
import sys

# The simulation output file contains two pieces of log. This script separate them
# 
# latency line 1 -> std err
# latency line 2 -> std out
# ...
# # (separator)
# log line 1
# log line 2
else_flag = False

for line in sys.stdin:
	if line is "#":
		else_flag = True
		continue
	if not else_flag: # latency lines
		sys.stderr.write(line + "\n")
	else:
		sys.stdout.write(line + "\n")

sys.stdout.flush()
sys.stderr.flush()