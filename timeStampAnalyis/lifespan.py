# -*- coding: utf-8 -*-
import sys
import io
import os.path

# It recieves a clean trace file
def operation(trace_line):
    return trace_line.split()[0] 

def time_and_pid(trace_line):
	splited = trace_line.split()
	pid = splited[-1]
	op = splited[0]
	if splited[0] == "open":
		return (long(splited[2]), long(pid))
	elif op == "close" or op == "read" or op == "write":
		return (long(splited[1]), long(pid))

def open_attrs(trace_line):
    #open	/bin		123			4	59
    #op		filename	timestamp	fd	pid
    (op, filename, timestamp, fd, pid) = trace_line.split()
    return (fd, filename, long(timestamp), pid)

def close_attrs(trace_line):
    #close	456			4	60
    #op		timestamp	fd	pid
    (op, timestamp, fd, pid) = trace_line.split()
    return (fd, long(timestamp), pid)

def dataops_attrs(trace_line):
    #read	789			3	0
    #write	789			3	104
    #op		timestamp	fd	pid
    (op, timestamp, fd, pid) = trace_line.split()
    return (fd, long(timestamp), pid)

def update_lifespan(filename, new_time, lifespan_dict):
    if not filename in lifespan_dict:
		lifespan_dict[filename] = (new_time, new_time)
    else:
		(old_firststamp, old_last_stamp) = lifespan_dict[filename]
		lifespan_dict[filename] = (old_firststamp, new_time)

def update_pid_lifespan(pid, new_time, pid_lifespan_dict):
    if not pid in pid_lifespan_dict:
		pid_lifespan_dict[pid] = (new_time, new_time)
    else:
		(old_firststamp, old_last_stamp) = pid_lifespan_dict[pid]
		pid_lifespan_dict[pid] = (old_firststamp, new_time)

openfd2filename = {}
files_lifespan = {}

pid2stamps = {} #{pid:(first_stamp, last_stamp)}

for line in sys.stdin:
	(timestamp, pid) = time_and_pid(line)
	update_pid_lifespan(pid, timestamp, pid2stamps)	
#	if 'open' == operation(line):
#		(fd, filename, time, pid) = open_attrs(line)
#		openfd2filename[fd] = filename
#		update_lifespan(filename, time, files_lifespan)
#	elif 'write' == operation(line) or 'read' == operation(line):
#		(fd, time, pid) = dataops_attrs(line)
#		filename = openfd2filename[fd]
#		update_lifespan(filename, time, files_lifespan)
#	elif 'close' == operation(line):
#		(fd, time, pid) = close_attrs(line)
#		filename = openfd2filename[fd]
#		update_lifespan(filename, time, files_lifespan)
#		del openfd2filename[fd]

#for (filename, lifespan) in files_lifespan.iteritems():
#    print "\t".join([filename, str(lifespan[0]), str(lifespan[1]), str(lifespan[1] -lifespan[0])])

for (pid, stamps) in pid2stamps.iteritems():
	pid_str = "\t".join([str(pid), str(stamps[0]), str(stamps[1]), str(stamps[1] - stamps[0])])
	sys.stdout.write(pid_str + "\n")

sys.stdout.flush()
sys.stdout.close()
