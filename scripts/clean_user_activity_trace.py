#!/usr/bin/python

import sys

last_timestamp=-1
line_number=0
clean=True

for line in sys.stdin:
	line_number = line_number + 1
	tokens = line.split()
	timestamp = int(tokens[0])
	inactivity_time = int(tokens[1]) / 1000

	if timestamp < last_timestamp:
		print "this trace remains unsorted"
		print line
		print str(line_number)
		clean=False
		break

	last_timestamp = timestamp
		
if clean:
	print "this trace is Ok"
else:
	print "this trace is not Ok"
