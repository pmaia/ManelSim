# -*- coding: iso-8859-1 -*-

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
#	elif op == "dup" or op == "dup2":
#		stamp = splited[1]
#		op_fd = splited[2]
#		op_fd2 = splited[3]
#		return (op, stamp, op_fd+":"+op_fd2, line)

"""  """
def sanitize_zero_fd(traceitem, nextitem):
	cleanlines = []
	# Cleaning open call with fd == 0
	(operation, stamp, fd, line) = traceitem
	if operation == 'open' and fd == '0':
		(next_op, next_stamp, next_fd, next_line) = nextitem
		if next_op == 'write' or next_op == 'read':
			# open    /etc/ld.so.cache    960254162.929000    3
			# modifying the original line and the original tuple
			splited = line.split()
			newline = splited[0] + '\t' + splited[1] + '\t' + splited[2] + '\t' + next_fd
			traceitem = (operation, stamp, next_fd, newline)
	return traceitem

"""  """
def general_sanitize(trace, verbose):
	fd_set = set()
	last_opened_fd = ""
	(already_in_use, read_a_non_open, write_a_non_open , close_a_non_open) = (0, 0, 0, 0)

	badstamp_counter = 0
	laststamp = 0

	for item in trace:
		splited = item.split()
		(operation, stamp, fd) = splited[0:3]
		line = "\t".join(splited[3:])
		stamp_as_time = long(stamp)
		if ( stamp_as_time >= laststamp):    # time increasing. good
			laststamp = stamp_as_time
			if operation == 'open':
				last_opened_fd = fd
				fd_set.add(fd)
				clean_trace.writelines(line.strip() + "\n")
			elif operation == 'close':
				if not fd in fd_set:
					close_a_non_open += 1
				else :
					fd_set.remove(fd)
					clean_trace.writelines(line.strip() + "\n")
			elif operation == 'read':
				if not fd in fd_set:
					read_a_non_open += 1
				else :
					clean_trace.writelines(line.strip() + "\n")
			elif operation == 'write':
				if not fd in fd_set:
					write_a_non_open += 1
				else :
					clean_trace.writelines(line.strip() + "\n")
#			elif operation == "dup" or operation == "dup2":
#				(old_fd, new_fd) = fd.split(":")
#				if old_fd in fd_set:
#					fd_set.add(new_fd)
#					clean_trace.writelines(line)				
		else :
			badstamp_counter += 1
			if verbose : print "stamp ", stamp_as_time, " old stamp ", laststamp, " bad: ", True

	if verbose :
		print "Remove lines by bad time stamp ", badstamp_counter
		print 'already_in_use ', already_in_use
		print 'read_a_non_open ', read_a_non_open
		print 'write_a_non_open ', write_a_non_open
		print 'close_a_non_open ', close_a_non_open

# Main script

bDict = {'false':False, 'true':True}
trace_path = sys.argv[1]
dirty_trace = open(trace_path, 'r')
 
# Reading from simplified trace file (only open, read, write and close operations)

sanit_trace = open(trace_path + '.sanit' , 'w')

duple = []
for line in dirty_trace:
	if not line.strip() == "" :
		if len(duple) == 2:			
			(operation, stamp, fd, line) = sanitize_zero_fd(duple[0], duple[1])
			str_p = "\t".join([operation, stamp, fd, line.strip()])
			sanit_trace.write(str_p + "\n")
			duple = []
		else:
			line_p = parse_line(line)
			duple.append(line_p)

sanit_trace.flush()
sanit_trace.close()
dirty_trace.close()

use_verbose = bDict[sys.argv[2]]
clean_trace = open(trace_path + '.clean' , 'w')
sanit_trace = open(trace_path + '.sanit' , 'r')
general_sanitize(sanit_trace, use_verbose)

clean_trace.flush()
clean_trace.close()
