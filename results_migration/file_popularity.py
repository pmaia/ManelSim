# -*- coding: utf-8 -*-
import sys
import io
import os.path

# It recieves a simulation output file like machine5-co-balance-workingset-50-true-1-1-5-24666.out

#[/home/thiago/Desktop/master/data_exper/machine5-simple.clean.clean, co-balance, 50, true, 1, true, 30]
#        29450403552     29468777916     10839382        10966164        2607246034      2631401392      729632  741520
#/h/users/bli/.netscape/|2kTTBY5K|/|MXFw|/|Al5THOR-Nv|           0       0       0       0       256     256     1       1

pGreatest = float(sys.argv[1])#[0,1]
mode = int(sys.argv[2]) #1 file2data, 2 file2ops, 3 numops2samples

file2data = {}
file2ops = {}
numops2samples = {}

lines = sys.stdin.readlines()
summary_line = lines[1].split()
sum_total_bytes = long(summary_line[1]) + long(summary_line[5])

for line in lines[2:]:
    if not line.strip() == "":
	tokens = line.split()
	bytes = long(tokens[2]) + long(tokens[6])
	ops = long(tokens[4]) + long(tokens[8])
	file2data[tokens[0]] = bytes
	file2ops[tokens[0]] = ops
	if not ops in numops2samples:
	    numops2samples[ops] = 1
	numops2samples[ops] = numops2samples[ops] + 1

list = sorted(file2data.iteritems(), key=lambda (k,v): (v,k))
n_values = int(pGreatest * len(list))
if mode == 1:
    cumm_prop = 0.0
    for (file, bytes) in list[len(list)-n_values:]:
	prop = float(bytes)/float(sum_total_bytes)
	cumm_prop += prop
	print prop
elif mode == 2:
    list = sorted(file2ops.iteritems(), key=lambda (k,v): (v,k))
    for (filename, ops) in list:
	print str(filename) + "\t" +  str(ops)
elif mode == 3:
    for (numops, samples) in numops2samples.iteritems():
	print str(numops) + "\t" + str(samples)
#print "the ", pGreatest, " most accessed files sums up", cumm_prop, " of the total accesses"