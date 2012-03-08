#!/usr/bin/python

# checks if events are ordered by timestamp in the raw idleness_logs

import sys

line = sys.stdin.readline()

previous_ts = int(line.split()[0])
lnumber = 1
for line in sys.stdin:
	lnumber = lnumber + 1
	ts = int(line.split()[0])
	if ts < previous_ts:
		print "erro in line: " + str(lnumber)
	previous_ts = ts
