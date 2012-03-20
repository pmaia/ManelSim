#!/usr/bin/python

# verifies if an idleness trace has the following properties:
#
#	1- timestamps of log entries are always growing
#	2- the module of the difference between two consecutive timestamps is almost always 1 (greater gaps can occur but it's something we need to take a look)
#	3- timestamps of log entries must appear only once (if the first property is enforced, this one is too)
#	4- when cumulated idleness time decreases, its value must never be greater than the difference between its timestamp and the previous one
#	5- when cumulated idleness time increases, its value must never be different from the sum of the previous cumulated idleness and the difference between timestamps
#	

import sys

previous_line = sys.stdin.readline()
line_number = 1
for line in sys.stdin:
	line_number = line_number + 1

	previous_ts = int(previous_line.split()[0])
	prev_idleness = int(previous_line.split()[1]) / 1000 #converting to seconds

	current_ts = int(line.split()[0])
	cur_idleness = int(line.split()[1]) / 1000 #converting to seconds
	
	ts_diff = current_ts - previous_ts

	if ts_diff <= 0:
		print 'error: back to past in line ' + str(line_number) + '. ts_diff = ' + str(ts_diff)
	elif ts_diff > 1:
		print 'warn: gap of ' + str(ts_diff) + ' seconds found in line ' + str(line_number)
	
	if ts_diff >= 1:
		if cur_idleness < prev_idleness:
			if cur_idleness > ts_diff:
				print 'error: cumulated idleness time is less than the previous one but greater than ts_diff in line ' + \
				str(line_number) + '. cur_idleness = ' + str(cur_idleness) + ', ts_diff = ' + str(ts_diff)
		elif cur_idleness > (prev_idleness + ts_diff + 1):
			print 'error: cumulated idleness time is greater than the previous one plus ts_diff in line ' + str(line_number) + \
			'. q. how big? a. ' + str(cur_idleness - (prev_idleness + ts_diff))
	previous_line = line


