#!/usr/bin/python

import os

lower = 1318967209 #idleness-celacanto
higher = 1320324040 #idleness-abelhinha

output_dir = "/local/patrickjem/traces_sbrc_final"

for dirname, dirnames, filenames in os.walk('/local/patrickjem/sbrc_experiment_traces'):
	for filename in filenames:
		trace_file = open(os.path.join(dirname, filename), "r")
		output_file = open(os.path.join(output_dir, filename), "w")
		if filename.startswith("idleness"):
			for line in trace_file:
				tokens = line.split()
				ts = int(tokens[1])
				if ts >= lower and ts <= higher:
					output_file.write(line)
		else:
			for line in trace_file:
				if line.startswith("#"):
					output_file.write(line)
				else:
					tokens = line.split()
					ts = int(tokens[1].split("-")[0]) / 1000000
					if ts >= lower and ts <= higher:
						output_file.write(line)

		trace_file.close()
		output_file.close()

			

