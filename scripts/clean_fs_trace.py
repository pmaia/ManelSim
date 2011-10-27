#!/usr/bin/python

# 1. keep only the calls to open, close, read, write and unlink
# 2. remove all unnecessary information like user id, process id, thread id, open flag, etc

import sys

global_opened_files_map = dict()
close_without_open_count = 0

# line sample from the original trace
#	uid pid tid exec_name sys_open begin-elapsed cwd filename flags mode return
#	0 2097 2097 (udisks-daemon) sys_open 1318539063003892-2505 / /dev/sdb 34816 0 7
# line transformed by clean_open
#	open	fullpath	begin-elapsed	fd
#	open	/dev/sdb	1318539063003892-2505	7
def clean_open(tokens):
	if not tokens[7].startswith('/'):
		fullpath = tokens[6] + tokens[7]
	else:
		fullpath = tokens[7]

	global_opened_files_map[tokens[10]] = fullpath #this information is usefull to the function clean_close

	if not fullpath.startswith("/home"):
		return None
	else:
		return "\t".join(['open', fullpath, tokens[5], tokens[10]])

# line sample from the original trace
#	uid pid tid exec_name sys_close begin-elapsed fd return
#	0 2097 2097 (udisks-daemon) sys_close 1318539063006403-37 7 0
# line transformed by clean_close
#	close	begin-elapsed	fd
#	close	1318539063006403-37	7
def clean_close(tokens):
	fullpath = global_opened_files_map.get(tokens[6], None)

	if fullpath == None:
		global close_without_open_count
		close_without_open_count = close_without_open_count + 1
	else:
		del global_opened_files_map[tokens[6]]

		if not fullpath.startswith("/home"):
			return None
		else:
			return "\t".join(['close', tokens[5], tokens[6]])

# line sample from the original trace: 
#	uid pid tid exec_name sys_write begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	0 6194 6194 (xprintidle) sys_write 1318539063058255-131 (/ /root/ /local/userActivityTracker/logs/tracker.log/ 10041417 S_IFREG|S_IROTH|S_IRGRP|S_IWUSR|S_IRUSR 2261065) 1 17 17
# line transformed by clean_write
#	write	begin-elapsed	fd	length
#	write	1318539063058255-131	1	17
def clean_write(tokens):
	if not tokens[8].startswith("/home"):
		return None
	else:
		return "\t".join(['write', tokens[5], tokens[12], tokens[14]])

# line sample from the original trace: 
#	uid pid tid exec_name sys_read begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	114 1562 1562 (snmpd) sys_read 1318539063447564-329 (/ / /proc/stat/ 0 S_IFREG|S_IROTH|S_IRGRP|S_IRUSR 4026531975) 8 3072 2971
# line transformed by clean_read
#	read	begin-elapsed	fd	length
#	read	1318539063447564-329	8	2971
def clean_read(tokens):
	if not tokens[8].startswith("/home"):
		return None
	else:
		return "\t".join(['read', tokens[5], tokens[12], tokens[14]])

# line sample from the original trace: 
#	uid pid tid exec_name sys_unlink begin-elapsed cwd pathname return
#	1159 2364 32311 (eclipse) sys_unlink 1318539134533662-8118 /home/thiagoepdc/ /local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png 0
# line transformed by clean_unlink
#	unlink	begin-elapsed	fullpath	
#	unlink	1318539134533662-8118	/local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png	
def clean_unlink(tokens):
	if not tokens[7].startswith('/'):
		fullpath = tokens[6] + tokens[7]
	else:
		fullpath = tokens[7]

	if not fullpath.startswith("/home"):
		return None
	else:
		return "\t".join(['unlink', tokens[5], fullpath])



for line in sys.stdin:
	tokens = line.split()

	clean_line = None
	if tokens[4] == 'sys_open':
		clean_line = clean_open(tokens)
	elif tokens[4] == 'sys_close':
		clean_line = clean_close(tokens)
	elif tokens[4] == 'sys_write':
		clean_line = clean_write(tokens)
	elif tokens[4] == 'sys_read':
		clean_line = clean_read(tokens)
	elif tokens[4] == 'sys_unlink':
		clean_line = clean_unlink(tokens)

	if clean_line != None:
		print clean_line

#print close_without_open_count
