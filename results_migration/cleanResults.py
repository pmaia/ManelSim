# -*- coding: utf-8 -*-
import sys
import io
import os.path

#	24038098623	29468777916	8745515	10966164	2217627626	2631401392	616367	741520
# should return this string without trailing blank characters and separeted by \t
def parseline(oldline):
    if oldline.strip() == "":
	return ""
    else:
    	splited = oldline.split()
    	print splited
    	return "\t".join(splited).strip()


trace_path = sys.argv[1]

if os.path.isfile(trace_path):
    
    trace = open(trace_path, 'r')
    clean_trace = open(trace_path+'.clean' , 'w')

    for line in trace:
        clean_trace.writelines(parseline(line) + "\n")

    trace.close()

    clean_trace.flush()
    clean_trace.close()
else:
    print "file does not exist:", trace_path
