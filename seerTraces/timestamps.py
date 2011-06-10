
import sys
import io

"""  """
def parse_line(line):
   splited = line.split()
   op = splited[0]
   if op == 'open':
       return splited[2]
   elif op == 'write':
       return splited[1]
   elif op == 'close':
       return splited[1]
   elif op == 'read':
       return splited[1]
   return None
  
"""  """
def fromString2Time(stamp):
   splited = stamp.split(".")
   return (long(splited[0]), long(splited[1]))
   
# Main script     
trace_path = sys.argv[1]
delta_secs = long(sys.argv[2])

trace = open(trace_path, 'r')

files_and_opening_times = {}

# Reading from cleaned trace file (only open operations)
last_stamp = 0

for line in trace:
   if not line.strip() == "" :
       parsed = parse_line(line)
       if not parsed is None:
           (seconds, micro) = fromString2Time(parsed)
           if ((seconds - last_stamp) > delta_secs):
               last_stamp = seconds    
               print last_stamp

trace.close()