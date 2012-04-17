#!/usr/bin/python

#increments in x the timestamps of the given range

import sys

if len(sys.argv) < 3:
	print "Usage: " + sys.argv[0] + " <time increment> <start line> [end line]"
	sys.exit(1)

increment = int(sys.argv[1])
start = int(sys.argv[2])
has_end_bound = False
if len(sys.argv) > 3:
	end = int(sys.argv[3])
	has_end_bound = True

line_count = 0

for line in sys.stdin:
	line_count = line_count + 1

	if line_count >= start and (not has_end_bound or line_count <= end):
		tokens = line.split()
		timestamp = int(tokens[0]) + increment
		idleness = tokens[1]
		print str(timestamp) + "\t" + idleness
	else:
		print line.strip()

