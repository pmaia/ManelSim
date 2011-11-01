#!/usr/bin/python

# 1. keep only the calls to close, read, write and unlink
# 2. remove all unnecessary information like user id, process id, thread id, open flag, etc

import sys

fdpid_to_fullpath = dict()
fullpath_to_filetype = dict()
bad_format_open = 0
bad_format_do_filp_open = 0
bad_format_close = 0
bad_format_read = 0
bad_format_write = 0
bad_format_unlink = 0

# line sample from the original trace
#	uid pid tid exec_name sys_open begin-elapsed cwd filename flags mode return
#	0 2097 2097 (udisks-daemon) sys_open 1318539063003892-2505 / /dev/sdb 34816 0 7
def handle_sys_open(tokens):
	if len(tokens) != 11:
		global bad_format_open
		bad_format_open = bad_format_open + 1
	else:
		if not tokens[7].startswith('/'):
			fullpath = tokens[6] + tokens[7]
		else:
			fullpath = tokens[7]
	
		unique_file_id =  tokens[10] + '-' + tokens[1]
	
		fdpid_to_fullpath[unique_file_id] = fullpath 

# line sample from the original trace
# uid pid tid exec_name do_filp_open begin-elapsed (root pwd fullpath f_size f_type ino) pathname openflag mode acc_mode
# 1159 2076 2194 (gnome-do) do_filp_open 1318539555109420-33 (/ /home/thiagoepdc/ /tmp/tmp2b688269.tmp/ 10485760 S_IFREG|S_IWUSR|S_IRUSR 12) <unknown> 32834 420 0
def handle_do_filp_open(tokens):
	if len(tokens) != 16:
		global bad_format_do_filp_open
		bad_format_do_filp_open = bad_format_do_filp_open + 1
	else:
		fullpath_to_filetype[tokens[8]] = tokens[10].split('|')[0]


# line sample from the original trace
#	uid pid tid exec_name sys_close begin-elapsed fd return
#	0 2097 2097 (udisks-daemon) sys_close 1318539063006403-37 7 0
# line transformed by clean_close
#	close	begin-elapsed	fullpath
#	close	1318539063006403-37	/local/userActivityTracker/logs/tracker.log/
def clean_close(tokens):
	if len(tokens) != 8:
		global bad_format_close
		bad_format_close = bad_format_close + 1
		return None;

	unique_file_id = tokens[6] + '-' + tokens[1]
	fullpath = fdpid_to_fullpath.get(unique_file_id, None)
	filetype = fullpath_to_filetype.get(fullpath, None)

	if fullpath != None and filetype != None:
		del fdpid_to_fullpath[unique_file_id]

		if fullpath.startswith("/home") and filetype == 'S_IFREG':
			return "\t".join(['close', tokens[5], fullpath])
		else:
			return None

# line sample from the original trace: 
#	uid pid tid exec_name sys_write begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	0 6194 6194 (xprintidle) sys_write 1318539063058255-131 (/ /root/ /local/userActivityTracker/logs/tracker.log/ 10041417 S_IFREG|S_IROTH|S_IRGRP|S_IWUSR|S_IRUSR 2261065) 1 17 17
# line transformed by clean_write
#	write	begin-elapsed	fullpath	length
#	write	1318539063058255-131	/local/userActivityTracker/logs/tracker.log/	17
def clean_write(tokens):
	if len(tokens) != 15:
		global bad_format_write
		bad_format_write = bad_format_write + 1
		unique_file_id = tokens[6] + '-' + tokens[1]
		fullpath = fdpid_to_fullpath.get(unique_file_id, None)
		filetype = fullpath_to_filetype.get(fullpath, None)
		length = tokens[8]
	else:
		unique_file_id = tokens[12] + '-' + tokens[1]
		fullpath = tokens[8]
		filetype = tokens[10].split('|')[0]
		length = tokens[14]

	if fullpath != None and filetype != None:
		fdpid_to_fullpath[unique_file_id] = fullpath
		fullpath_to_filetype[fullpath] = filetype
		if fullpath.startswith("/home") and filetype == 'S_IFREG':
			return "\t".join(['write', tokens[5], fullpath, length])
		else:
			return None
	else:
		return None

# line sample from the original trace: 
#	uid pid tid exec_name sys_read begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	114 1562 1562 (snmpd) sys_read 1318539063447564-329 (/ / /proc/stat/ 0 S_IFREG|S_IROTH|S_IRGRP|S_IRUSR 4026531975) 8 3072 2971
# line transformed by clean_read
#	read	begin-elapsed	fullpath	length
#	read	1318539063447564-329	/proc/stat/	2971
def clean_read(tokens):
	if len(tokens) != 15:
		global bad_format_read
		bad_format_read = bad_format_read + 1
		unique_file_id = tokens[6] + '-' + tokens[1]
		fullpath = fdpid_to_fullpath.get(unique_file_id, None)
		filetype = fullpath_to_filetype.get(fullpath, None)
		length = tokens[8]
	else:
		unique_file_id = tokens[12] + '-' + tokens[1]
		fullpath = tokens[8]
		filetype = tokens[10].split('|')[0]
		length = tokens[14]

	if fullpath != None and filetype != None:
 		fdpid_to_fullpath[unique_file_id] = fullpath
		fullpath_to_filetype[fullpath] = filetype
		if fullpath.startswith("/home") and filetype == 'S_IFREG':
			return "\t".join(['read', tokens[5], fullpath, length])
		else:
			return None
	else:
		return None

# line sample from the original trace: 
#	uid pid tid exec_name sys_unlink begin-elapsed cwd pathname return
#	1159 2364 32311 (eclipse) sys_unlink 1318539134533662-8118 /home/thiagoepdc/ /local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png 0
# line transformed by clean_unlink
#	unlink	begin-elapsed	fullpath	
#	unlink	1318539134533662-8118	/local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png	
def clean_unlink(tokens):
	if len(tokens) != 0:
		global bad_format_unlink
		bad_format_unlink = bad_format_unlink + 1
		return None;

	if not tokens[7].startswith('/'):
		fullpath = tokens[6] + tokens[7]
	else:
		fullpath = tokens[7]

	if not fullpath.startswith("/home"):
		return None
	else:
		return "\t".join(['unlink', tokens[5], fullpath])

### Main ###

for line in sys.stdin:
	tokens = line.split()

	clean_line = None
	if tokens[4] == 'sys_open':
		clean_line = handle_sys_open(tokens)
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

print '# number of bad formatted reads, writes, opens, closes and unlinks'
print '# reads:\t' + str(bad_format_read)
print '# writes:\t' + str(bad_format_write)
print '# opens:\t' + str(bad_format_open)
print '# do filp opens:\t' + str(bad_format_do_filp_open)
print '# closes:\t' + str(bad_format_close)
print '# unlinks:\t' + str(bad_format_unlink)

### End ###
