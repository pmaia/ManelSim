#!/usr/bin/python

import sys

line = sys.stdin.readline()
tokens = line.split()
previous_timestamp = int(tokens[0])
previous_idleness_time = int(tokens[1]) / 1000

for line in sys.stdin:
	tokens = line.split()
	timestamp = int(tokens[0])
	idleness_time = int(tokens[1]) / 1000
	
	time_between_logs = timestamp - previous_timestamp

	#there was a cron job that should initialize the idleness tracker every 5 minutes in case it was not still running, so
	if time_between_logs > 6 * 60 :
		print "shutdown\t" + str(previous_timestamp) + "\t" + str(time_between_logs - idleness_time)
	elif idleness_time < previous_idleness_time:
		print "idleness\t" + str(previous_timestamp - previous_idleness_time) + "\t" + str(previous_idleness_time)
	
	previous_timestamp = timestamp
	previous_idleness_time = idleness_time

print "idleness\t" + str(timestamp - idleness_time) + "\t" + str(idleness_time)
