#!/usr/bin/python

import sys

tokens = sys.stdin.readline().split()

previous_ts = int(tokens[1])
supposed_ts = previous_ts + int(tokens[2])

line_count = 0
for line in sys.stdin:
	line_count = line_count + 1
	tokens = line.split()
	actual_ts = int(tokens[1])
	duration = int(tokens[2])

	if previous_ts > actual_ts:
		print "unordered event in line: " + str(line_count)
	
	if duration < 0:
		print "negative duration in line: " + str(line_count)

	if actual_ts != supposed_ts:
		print "incoerent timestamp in line: " + str(line_count) + ". timestamp is " + str(actual_ts) + " but should be " + str(supposed_ts)

	supposed_ts = actual_ts + duration
	previous_ts = actual_ts
