#!/usr/bin/python

import sys

#keeps only one line for each unique timestamp

tokens = sys.stdin.readline().split()
previous_timestamp = int(tokens[0])
previous_duration = int(tokens[1])

for line in sys.stdin:
	tokens = line.split()
	timestamp = int(tokens[0])
	duration = int(tokens[1])
	if timestamp != previous_timestamp:
		print str(previous_timestamp) + "\t" + str(previous_duration)

	previous_timestamp = timestamp
	previous_duration = duration


print str(previous_timestamp) + "\t" + str(previous_duration)
