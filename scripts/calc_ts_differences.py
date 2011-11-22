#!/usr/bin/python

import sys

line = sys.stdin.readline()
previous_timestamp = int(line.split()[0])

for line in sys.stdin:
	timestamp = int(line.split()[0])
	print timestamp - previous_timestamp
	previous_timestamp = timestamp
