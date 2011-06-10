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
def sanitize_zero_fd(listlines):
	cleanlines = []
	# Cleaning open call with fd == 0
	for index in range(len(listlines)):
		traceitem = listlines[index]
		(operation, stamp, fd, line) = traceitem
		if operation == 'open' and fd == '0' and (index < (len(listlines) - 1)) :
			(next_op, next_stamp, next_fd, next_line) = listlines[index + 1]
			if next_op == 'write' or next_op == 'read':
				# open    /etc/ld.so.cache    960254162.929000    3
				# modifying the original line and the original tuple
				splited = line.split()
				newline = splited[0] + '\t' + splited[1] + '\t' + splited[2] + '\t' + next_fd
				traceitem = (operation, stamp, next_fd, newline)
		cleanlines.append(traceitem)
	return cleanlines

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

"""  """
def general_sanitize(listline, verbose):
	fd_set = set()
	last_opened_fd = ""
	(already_in_use, read_a_non_open, write_a_non_open , close_a_non_open) = (0, 0, 0, 0)

	badstamp_counter = 0
	laststamp = (0, 0)

	for item in listline:
		(operation, stamp, fd, line) = item
		stamp_as_time = fromString2Time(stamp)
		if (compare_stamps(stamp_as_time, laststamp) >= 0):    # time increasing. good
			laststamp = stamp_as_time
			if operation == 'open':
				last_opened_fd = fd
				fd_set.add(fd)
				clean_trace.writelines(line)
			elif operation == 'close':
				if not fd in fd_set:
					close_a_non_open += 1
				else :
					fd_set.remove(fd)
					clean_trace.writelines(line)
			elif operation == 'read':
				if not fd in fd_set:
					read_a_non_open += 1
				else :
					clean_trace.writelines(line)
			elif operation == 'write':
				if not fd in fd_set:
					write_a_non_open += 1
				else :
					clean_trace.writelines(line)
#			elif operation == "dup" or operation == "dup2":
#				(old_fd, new_fd) = fd.split(":")
#				if old_fd in fd_set:
#					fd_set.add(new_fd)
#					clean_trace.writelines(line)				
		else :
			badstamp_counter += 1
			if verbose : print "stamp ", stamp_as_time, "bad: ", True

	if verbose :
		print "Remove lines by bad time stamp ", badstamp_counter
		print 'already_in_use ', already_in_use
		print 'read_a_non_open ', read_a_non_open
		print 'write_a_non_open ', write_a_non_open
		print 'close_a_non_open ', close_a_non_open

# Main script

bDict = {'false':False, 'true':True}
trace_path = sys.argv[1]
trace = open(trace_path, 'r')

# Reading from simplified trace file (only open, read, write and close operations)
lines = []
for line in trace:
	if not line.strip() == "" :
		lines.append(parse_line(line))

clean_trace = open(trace_path + '.clean' , 'w')

use_verbose = bDict[sys.argv[2]]

lines = sanitize_zero_fd(lines) 
lines = general_sanitize(lines, use_verbose)

clean_trace.flush()
clean_trace.close()

trace.close()
