# -*- coding: utf-8 -*-

# The seer trace has some troubles. One of them is read and write operations using 
# an invalid file_descriptor. A common case is using an closed fd_set.
# This code removes the inconsistent lines, taking as parameter a 'xpto.simple' input file

import sys
import io

"""  """
def parse_line(line):
   splited = line.split()
   op = splited[0]
   if op == 'open':
       filename = splited[1]
       stamp = splited[2]
       return (filename, stamp)
   else:
       return None
  
"""  """ 
def compare_stamps(stamp_one, stamp_two):
   (one_secs, one_micro) = stamp_one
   (two_secs, two_micro) = stamp_two
    
   diff_secs = one_secs - two_secs
   if diff_secs == 0:
       return one_micro - two_micro
   else:
       return diff_secs
       
"""  """
def fromString2Time(stamp):
   splited = stamp.split(".")
   return (long(splited[0]), long(splited[1]))
   
def shouldExclude(path):
   return path.startswith("/var") or path.startswith("/tmp/") or path.startswith("/usr") or path.startswith("/etc") or path.startswith("/dev") or path.startswith("/sbin") or path.startswith("/proc") or path.startswith("/lib")

# Main script     
trace_path = sys.argv[1]
trace = open(trace_path, 'r')

files_and_opening_times = {}

# Reading from cleaned trace file (only open operations)
for line in trace:
   if not line.strip() == "" :
       parsed = parse_line(line)
       if not parsed is None:
	   (filename, stamp) = parsed
	   stamp_as_time = fromString2Time(stamp)
	   if not filename in files_and_opening_times:
	       files_and_opening_times[filename] = []
	   files_and_opening_times[filename].append(stamp_as_time[0])

allitems = 0
excludeditems = 0

alltrace = open("pattern.all", 'w')
excluded = open("pattern.excluded", 'w')

for (key, value) in files_and_opening_times.items():    
   allitems += len(value)
   if shouldExclude(key):
       excludeditems += len(value)
   else:       
       excluded.writelines(key + " " + str(len(value)) + " " + str((value[-1] - value[0])) + "\n")
   alltrace.writelines(key + " " + str(len(value)) + " " + str((value[-1] - value[0])) + "\n")

print "excluded: " + str(excludeditems) + " all: " + str(allitems)

alltrace.flush()
alltrace.close()
excluded.flush()
excluded.close()

trace.close()
