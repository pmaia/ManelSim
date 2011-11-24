#!/usr/bin/python

import sys
import cmath

line = sys.stdin.readline()
tokens = line.split()
previous_timestamp = int(tokens[0])
previous_idleness_time = int(tokens[1]) / 1000
pending_shutdown = False

def equals(value1, value2, err):
	return abs(value1 - value2) <= err 

for line in sys.stdin:
	tokens = line.split()
	timestamp = int(tokens[0])
	idleness_time = int(tokens[1]) / 1000
	
	time_between_logs = timestamp - previous_timestamp

	if time_between_logs > 1:
		if (time_between_logs - idleness_time < 6 * 60 or # (the gap is too small) 
                    equals(idleness_time, previous_idleness_time + time_between_logs, 1)): # (it is a continuation of the previous idleness)
			previous_timestamp = timestamp - 1
			previous_idleness_time = previous_idleness_time + time_between_logs - 1
		else: 
			print "idleness\t" + str(previous_timestamp - previous_idleness_time) + "\t" + str(previous_idleness_time)
			print "shutdown\t" + str(previous_timestamp) + "\t" + str(time_between_logs - idleness_time)
			previous_timestamp = timestamp
			previous_idleness_time = idleness_time
			continue
	
	if idleness_time < previous_idleness_time:
		print "idleness\t" + str(previous_timestamp - previous_idleness_time) + "\t" + str(previous_idleness_time)

	previous_timestamp = timestamp
	previous_idleness_time = idleness_time

print "idleness\t" + str(timestamp - idleness_time) + "\t" + str(idleness_time)
