# -*- coding: utf-8 -*-
import sys
import io
import os.path

# It recieves the output of lifespan.py
#v2.2-06292000-bad/drivers/cdrom/cm206.h,v	1	82

def extract_lifespan(trace_line):
    tokens = trace_line.split()
    return long(tokens[2]) - long(tokens[1]) 

lifespan_freqs = {}

for line in sys.stdin:
    lifespan = extract_lifespan(line)
    if not lifespan in lifespan_freqs:
	lifespan_freqs[lifespan] = 0
    lifespan_freqs[lifespan] = lifespan_freqs[lifespan] + 1
    
for (lifespan, freq) in lifespan_freqs.iteritems():
    print "\t".join([str(lifespan), str(freq)])