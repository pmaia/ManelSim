#!/usr/bin/python

import sys

if len(sys.argv) < 3:
	print 'usage: ' + sys.argv[0] + ' <machine_id> <machine_name>'
	sys.exit(1)


previous_line = sys.stdin.readline()

reliable_ts = None
failed = False

machine_id = sys.argv[1]
machine_name = sys.argv[2]

for line in sys.stdin:

	previous_ts = int(previous_line.split()[0])
	prev_idleness = int(previous_line.split()[1]) / 1000

	if reliable_ts == None:
		reliable_ts = previous_ts

	current_ts = int(line.split()[0])
	cur_idleness = int(line.split()[1]) / 1000
	
	ts_diff = current_ts - previous_ts

	if ts_diff <= 0:
		failed = True
	elif ts_diff > 1:
		failed = True
	
	if ts_diff >= 1:
		if cur_idleness < prev_idleness:
			if cur_idleness > ts_diff:
				failed = True
		elif cur_idleness > (prev_idleness + ts_diff + 1):
				failed = True
	
	if failed:
		print str(reliable_ts) + '\t' + machine_id + '\t' + machine_name
	else:
		print str(reliable_ts) + '\t0\t' + machine_name

	previous_line = line
	failed = False
	reliable_ts = reliable_ts + 1

