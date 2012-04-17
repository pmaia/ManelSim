#!/usr/bin/python

import sys

uniq_lines = dict()

for line in sys.stdin:
	tokens = line.split()
	ts = tokens[0]
	idleness = int(tokens[1])/1000
	transformed_line = ts + '\t' + str(idleness)
	if transformed_line not in uniq_lines:
		print line.strip()
		uniq_lines[transformed_line] = 1
