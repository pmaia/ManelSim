# -*- coding: utf-8 -*-
import sys
import io
import os.path

sim_out = open(sys.argv[1], 'r')
migration = open(sys.argv[2], 'r')

file2data = {}

lines = sim_out.readlines()
summary_line = lines[1].split()
sum_total = long(summary_line[1]) + long(summary_line[5])

for line in lines[2:]:
    if not line.strip() == "":
	tokens = line.split()
	total = long(tokens[2]) + long(tokens[6])
	file2data[tokens[0]] = total

migrated2data = {}
migrated2time = {}
for migration_line in migration:
    if not migration_line.strip() == "":
	(token,touch_time, migration_time, name) = migration_line.split()
	migrated2data[name] = float(file2data[name])/float(sum_total)
	migrated2time[name] = (touch_time, migration_time)

list = sorted(migrated2data.iteritems(), key=lambda (k,v): (v,k))
for (filename, proportion) in list:
    print filename, proportion, touch_time, migrated2time[filename]