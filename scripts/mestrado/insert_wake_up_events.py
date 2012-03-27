#!/usr/bin/python

import sys

prev_line = sys.stdin.readline();
tokens = prev_line.split()
prev_timestamp = int(tokens[1])
prev_duration = int(tokens[2])

activity_start = None
for line in sys.stdin:

	tokens = line.split()
	timestamp = int(tokens[1])
	duration = int(tokens[2])

	new_event_start = prev_timestamp + prev_duration
	new_event_duration = timestamp - new_event_start

	if new_event_duration > 5 * 60: #a gap of this size is not activity, but probably a shutdown
		prev_duration = prev_duration + new_event_duration + duration #lets just consider it was idle during this time
	else:
		print "idleness\t" + str(prev_timestamp) + "\t" + str(prev_duration)
		print "activity\t" + str(new_event_start) + "\t" + str(new_event_duration)
		prev_timestamp = timestamp
		prev_duration = duration

print "idleness\t" + str(prev_timestamp) + "\t" + str(prev_duration)
