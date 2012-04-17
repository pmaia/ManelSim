#!/usr/bin/python

import sys

prev_timestamp = int(sys.stdin.readline()[5))
line_count = 1
for line in sys.stdin:
	line_count = line_count + 1
	timestamp = int(line.split()[5])

	if timestamp < prev_timestamp:
		print "back in time. line " + str(line_count)

