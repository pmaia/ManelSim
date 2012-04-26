#!/usr/bin/python

import sys

def extract_time(line):
	time_str = line.split()[1].split("-")[0]
	return int(time)

def main():
	prev_timestamp = extract_time(sys.stdin.readline())
	line_count = 1
	for line in sys.stdin:
		line_count += 1
		timestamp = extract_time(line) 

		if timestamp < prev_timestamp:
			print "back in time. line " + str(line_count)

if __name__ == "__main__":
	main()
