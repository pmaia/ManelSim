#!/usr/bin/python

import sys

def extract_time(line):
	time_str = line.split()[1].split("-")[0]
	return int(time_str)

def main():
	prev_timestamp = extract_time(sys.stdin.readline())
	line_count = 1
	for line in sys.stdin:
		line_count += 1
		if not line.startswith("#"):
			timestamp = extract_time(line) 

			ts_diff = (timestamp - prev_timestamp) / 1000000
			if ts_diff < -10:
				print " ".join(["ERROR", str(ts_diff * -1), str(line_count), line.strip()])
			elif ts_diff < 0:
				print " ".join(["WARNING", str(ts_diff * -1), str(line_count), line.strip()])

			prev_timestamp = timestamp

if __name__ == "__main__":
	main()
