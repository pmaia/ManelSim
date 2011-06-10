import sys
import io

def parsetime(time):
   return long(time)

trace_path = sys.argv[1]
minute = long(sys.argv[2])

trace = open(trace_path, 'r')

first = -1

# 1 minute = 60 * 1000 (milli)

dict = {}
index = 0
for line in trace:
    stamp = parsetime(line)
    if (first == -1) :
        first = stamp
    delta = stamp - first
    index = delta / minute
    if index not in dict:
        dict[index] = 0
    dict[index] = dict[index] + 1

for i in range(0, index + 1):
    if i not in dict:
        dict[i] = 0
    print i, "\t", dict[i]

trace.close()
