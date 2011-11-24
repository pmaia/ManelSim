#!/usr/bin/python

import sys

line = sys.stdin.readline()
tokens = line.split()
previous_timestamp = int(tokens[0])
previous_idleness_time = int(tokens[1]) / 1000
pending_shutdown = False

for line in sys.stdin:
	tokens = line.split()
	timestamp = int(tokens[0])
	idleness_time = int(tokens[1]) / 1000
	
	time_between_logs = timestamp - previous_timestamp

	if time_between_logs > 1:  
	# The tracker for some reason didn't log during some seconds. 
	# We need to decide if the machine was turned off or just idle during this time. 
	# we consider just these two states because they are the pessimistic scenarios for the hypothesis we think is true.
	#
	# During the time the trace was being collected there was a cron job that should initialize the idleness tracker 
	# at most after 5 minutes from the last check.
	# Because of that, we think it is reasonable to consider that in case of the gap duration is less than 6 minutes, 
	# the machine was on but the tracker was not or it was on but restarting. 
	# So, lets adjust things to look like a normal idleness in this case...

		previous_timestamp = timestamp - 1
		previous_idleness_time = previous_idleness_time + time_between_logs - 1

		if time_between_logs > 6 * 60: # and if the gap is too big, we consider it as a shutdown
			pending_shutdown = True			
	
	if idleness_time < previous_idleness_time:
		start =  str(previous_timestamp - previous_idleness_time) 
		duration = str(previous_idleness_time + time_between_logs)

		if pending_shutdown:
			print "shutdown\t" + start + "\t" + duration
			pending_shutdown = False
		else:
			print "idleness\t" + start + "\t" + duration

	previous_timestamp = timestamp
	previous_idleness_time = idleness_time

print "idleness\t" + str(timestamp - idleness_time) + "\t" + str(idleness_time)
