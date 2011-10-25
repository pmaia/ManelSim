#!/usr/bin/python

# 1. converts logged inactivity periods from milliseconds to seconds
# 2. keeps only the line that records the maximum inactivity time per inactivity period 
# 3. the timestamp now refers to the moment the inactivity period started 

import sys

last_inactivity_time_read=-1
last_timestamp_read=-1
for line in sys.stdin:
	tokens = line.split()
	timestamp = int(tokens[0])
	inactivity_time = int(tokens[1]) / 1000

	if inactivity_time < last_inactivity_time_read:
		print str(last_timestamp_read - last_inactivity_time_read) + "\t" + str(last_inactivity_time_read)

	last_inactivity_time_read = inactivity_time
	last_timestamp_read = timestamp

print str(last_timestamp_read - last_inactivity_time_read) + "\t" + str(last_inactivity_time_read)
