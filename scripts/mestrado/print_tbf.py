#!/usr/bin/python

import sys

previous_line = sys.stdin.readline()

last_failure = None

def report_failure():
	global last_failure
	print str(previous_ts - last_failure)
	last_failure = current_ts

for line in sys.stdin:

	previous_ts = int(previous_line.split()[0])
	prev_idleness = int(previous_line.split()[1]) / 1000

	if last_failure == None:
		last_failure = previous_ts

	current_ts = int(line.split()[0])
	cur_idleness = int(line.split()[1]) / 1000
	
	ts_diff = current_ts - previous_ts

	if ts_diff <= 0:
		report_failure()
	elif ts_diff > 1:
		report_failure()
	
	if ts_diff >= 1:
		if cur_idleness < prev_idleness:
			if cur_idleness > ts_diff:
				report_failure()
		elif cur_idleness > (prev_idleness + ts_diff):
				report_failure()
	
	previous_line = line


