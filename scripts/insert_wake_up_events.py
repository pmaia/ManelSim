#!/usr/bin/python

import sys

activity_start = None
for line in sys.stdin:

	tokens = line.split()
	timestamp = int(tokens[1])
	duration = int(tokens[2])

	if activity_start != None:
		if not line.startswith("shutdown"):
			print previous_line.strip()
			print "activity\t" + str(activity_start) + "\t" + str(timestamp - activity_start)
			activity_start = timestamp + duration
			previous_line = line
		else:
			print previous_line.strip()
			print line.strip()
			activity_start = None
	else:
		activity_start = timestamp + duration
		previous_line = line

