# -*- coding: iso-8859-1 -*-

import sys
import io

""" """
def shouldExclude(path):
   return path.startswith("/var") or path.startswith("/tmp/") or path.startswith("/usr") or \
  	 path.startswith("/etc") or path.startswith("/dev") or path.startswith("/sbin") or \
  	  path.startswith("/proc") or path.startswith("/lib")

"""  """
def parse_line(line):
   #print "line: ", line	
   splited = line.split()
   op = splited[0]
   if op == 'open':
       stamp = splited[2]
       op_fd = splited[3]
       return (op, stamp, op_fd, line)
   elif op == 'close':
       stamp = splited[1]
       op_fd = splited[2]
       return ('close', stamp, op_fd, line) 
   elif op == 'read':
       stamp = splited[1]
       op_fd = splited[2]
       return ('read', stamp, op_fd, line)
   elif op == 'write':
       stamp = splited[1]
       op_fd = splited[2]
       return ('write', stamp, op_fd, line)

# Main script
# Reading from simplified trace file (only open, read, write and close operations)
bad_fds = set()

for item in sys.stdin:
	if not item.strip() == "" :
		(op, stamp, op_fd, line) = parse_line(item)
		if op == 'open':
			filename = line.split()[1]
			if shouldExclude(filename):
				bad_fds.add(op_fd)
			else:
				print line.strip()
		elif op == 'close':
			if op_fd in bad_fds:
				bad_fds.remove(op_fd)
			else:
				print line.strip()
		elif op == 'read':
			if op_fd not in bad_fds:
				print line.strip()
		elif op == 'write':
			if op_fd not in bad_fds:
				print line.strip()