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

	if time_between_logs > 1:  
	#the tracker for some reason didn't log during some seconds. 
	#we need to decide if the machine was sleeping (shutdown) or just idle during this time. 
	#we consider just these two states because they are the pessimistic scenarios for the hypothesis we think is true
		if time_between_logs < 6 * 60:
		#there was a cron job that should initialize the idleness tracker every 5 minutes in case it was not still running.
		#because of that, we think it is reasonable to consider that in case of the gap duration is less than 6 minutes, 
		#the machine was on but the tracker was not or it was on but restarting. So, lets adjust things to look like a normal idleness...
			previous_timestamp = timestamp - 1
			previous_idleness_time = previous_idleness_time + time_between_logs - 1
			
			if idleness_time < previous_idleness_time:
				print "idleness\t" + str(previous_timestamp - previous_idleness_time) + "\t" + str(previous_idleness_time + time_between_logs)

		else:
		#the possibility that idleness_time is a continuation of previous_idleness_time still remains. we need to check
			if idleness_time - previous_idleness_time <= time_between_logs:
				print "shutdown\t" + str(previous_timestamp + previous_idleness_time) + "\t" + str(time_between_logs - idleness_time)
#			else:
#				do nothing, it is a continuation of a previous idleness period (I'm not sure if this is true)
	
	elif idleness_time < previous_idleness_time:
		print "idleness\t" + str(previous_timestamp - previous_idleness_time) + "\t" + str(previous_idleness_time + time_between_logs)

	previous_timestamp = timestamp
	previous_idleness_time = idleness_time

print "idleness\t" + str(timestamp - idleness_time) + "\t" + str(idleness_time)
