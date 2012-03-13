#!/usr/bin/python

# fix error of 1 second in timestamp log

import sys

TRACKER_TS_ERROR = 2

known_error = TRACKER_TS_ERROR
previous_line = sys.stdin.readline()
line_number = 1
for line in sys.stdin:
	line_number = line_number + 1

	previous_ts = int(previous_line.split()[0])

	current_ts = int(line.split()[0])
	cur_idleness = int(line.split()[1]) 
	
	ts_diff = current_ts - previous_ts

	if ts_diff == 0 or (ts_diff > 1 and ts_diff <= known_error):
		current_ts = previous_ts + 1
		line = str(current_ts) + '\t' + str(cur_idleness)

		known_error = max(ts_diff + 1, TRACKER_TS_ERROR)

	print previous_line.strip()

	previous_line = line

