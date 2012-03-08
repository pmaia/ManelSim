#!/usr/bin/python

import sys

diff_map = dict()

try:
	line = sys.stdin.readline()
	previous_timestamp = int(line.split()[0])

	for line in sys.stdin:
		timestamp = int(line.split()[0])
		diff = timestamp - previous_timestamp
		previous_timestamp = timestamp

		if diff_map.has_key(diff):
			diff_map[diff] = diff_map[diff] + 1
		else:
			diff_map[diff] = 1
finally:
	for diff, freq in diff_map.iteritems():
		print str(diff) + "\t-> " + str(freq)
