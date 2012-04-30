#!/usr/bin/python

# 1. keep only the calls to close, read, write and unlink
# 2. remove all unnecessary information like user id, process id, thread id, open flag, etc

import sys
from clean_fs_utils import *

fdpid_to_fullpath = dict()
fullpath_to_filetype = dict()
fullpath_to_filesize = dict()

def remove_basedir_if_present(fullpath):
	index_of_double_slashes = fullpath.find("/ /")
	if index_of_double_slashes != -1:
		fullpath = fullpath[index_of_double_slashes + 2:]
	return fullpath

def check_filetype(tokens):
	filetype = tokens[-5].split('|')[0] 

	if filetype == 'sys_read' or filetype == 'sys_write': 
	#so, it's missing all the stuff between parentheses and I'll try to recover it
		fdpid = "-".join([tokens[-3], tokens[1]])
		filetype = fullpath_to_filetype[fdpid_to_fullpath[fdpid]]

	return filetype == 'S_IFREG'
	
def check_get_bytes_transfered(tokens):
	return int(tokens[-1])

def check_get_filesize(tokens): 
	try:
		filesize = int(tokens[-6])
	except (ValueError):
		fdpid = "-".join([tokens[-3], tokens[1]])
		filesize = int(fullpath_to_filesize[fdpid_to_fullpath[fdpid]])

	if filesize < 0:
		raise Exception("Invalid or missing filesize")

	return str(filesize)

def check_get_begin_elapsed(tokens):
	begin_elapsed = tokens[5]
	operation_start = int(begin_elapsed.split("-")[0])
	operation_duration = int(begin_elapsed.split("-")[1])
	
	if operation_start < 0 or operation_duration < 0:
		raise Exception("Invalid start time and/or duration")

	return begin_elapsed

def check_get_fullpath(tokens):
	fdpid = '-'.join([tokens[-3], tokens[1]])
	
	if len(tokens) >= 15:
		# So, this is a well formed line or the universe is conspiring against me
		fullpath = remove_basedir_if_present(" ".join(tokens[7:len(tokens)-6]))
		
		if fdpid in fdpid_to_fullpath:
			if fdpid_to_fullpath[fdpid] != fullpath:
				sys.stderr.write(" ".join(["WARNING:", "Missing close detected.", fdpid, "Path:", fullpath, "\n"]))
		else:
			sys.stderr.write(" ".join(["WARNING:", "Missing open detected.", fdpid, "Path:", fullpath, "\n"]))

		fdpid_to_fullpath[fdpid] = fullpath
		fullpath_to_filetype[fullpath] = tokens[-5].split('|')[0]
		fullpath_to_filesize[fullpath] = tokens[-6]
	else:
		fullpath = fdpid_to_fullpath.get(fdpid, None) 
		# The probability of this information be wrong is not 0. 
		# But I believe it's really low. 
		# It would be necessary that a call to close were missing to a certain fd-pid pair and 
		# that the subsequent calls to sys_write/sys_read that coincidentally have the same fd-pid were bad formed.

	if fullpath == None:
		raise Exception("Irrecoverable bad formed line")

	return fullpath

# line sample from the original trace
# uid pid tid exec_name do_filp_open begin-elapsed (root pwd fullpath f_size f_type ino) pathname openflag mode acc_mode
# 1159 2076 2194 (gnome-do) do_filp_open 1318539555109420-33 (/ /home/thiagoepdc/ /tmp/tmp2b688269.tmp/ 10485760 S_IFREG|S_IWUSR|S_IRUSR 12) <unknown> 32834 420 0
def handle_do_filp_open(tokens):
	if len(tokens) < 16:
		raise Exception("missing tokens in do_filp_open")

	fullpath = remove_basedir_if_present(" ".join(tokens[7:len(tokens)-7]))

	fullpath_to_filetype[fullpath] = tokens[-6].split('|')[0]
	fullpath_to_filesize[fullpath] = tokens[-7]


# line sample from the original trace
# uid pid tid exec_name sys_open begin-elapsed cwd filename flags mode return
# 0 2097 2097 (udisks-daemon) sys_open 1318539063003892-2505 / /dev/sdb 34816 0 7
def handle_sys_open(tokens):
	if len(tokens) < 11:
		raise Exception("missing tokens in sys_open")
	
	fullpath = remove_basedir_if_present(" ".join(tokens[6:len(tokens)-3]))
	unique_file_id = "-".join([tokens[-1], tokens[1]])
	fdpid_to_fullpath[unique_file_id] = fullpath 

# line sample from the original trace
#	uid pid tid exec_name sys_close begin-elapsed fd return
#	0 2097 2097 (udisks-daemon) sys_close 1318539063006403-37 7 0
# line transformed by clean_close
#	close	begin-elapsed	fullpath
#	close	1318539063006403-37	/local/userActivityTracker/logs/tracker.log/
def clean_close(tokens):
	if len(tokens) != 8:
		raise Exception("missing tokens in sys_close")

	unique_file_id = tokens[6] + '-' + tokens[1]
	fullpath = fdpid_to_fullpath.get(unique_file_id, None)
	filetype = fullpath_to_filetype.get(fullpath, None)
	begin_elapsed = check_get_begin_elapsed(tokens)

	if fullpath != None:
		del fdpid_to_fullpath[unique_file_id]
		if fullpath.startswith("/home") and filetype == 'S_IFREG':
			return "\t".join(['close', begin_elapsed, fullpath])
	
	return None

# line sample from the original trace: 
#	uid pid tid exec_name sys_write begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	0 6194 6194 (xprintidle) sys_write 1318539063058255-131 (/ /root/ /local/userActivityTracker/logs/tracker.log/ 10041417 S_IFREG|S_IROTH|S_IRGRP|S_IWUSR|S_IRUSR 2261065) 1 17 17
# line transformed by clean_read_write
#	write	begin-elapsed	fullpath	bytes_transfered	filesize
#	write	1318539063058255-131	/local/userActivityTracker/logs/tracker.log/	17	10041417
# 
#	or, if it's a sys_read call:
#
# line sample from the original trace: 
#	uid pid tid exec_name sys_read begin-elapsed (root pwd fullpath f_size f_type ino) fd count return
#	114 1562 1562 (snmpd) sys_read 1318539063447564-329 (/ / /proc/stat/ 0 S_IFREG|S_IROTH|S_IRGRP|S_IRUSR 4026531975) 8 3072 2971
# line transformed by clean_read_write
#	read	begin-elapsed	fullpath	bytes_transfered
#	read	1318539063447564-329	/proc/stat/	2971

def clean_read_write(tokens):
	if check_filetype(tokens):
		begin_elapsed = check_get_begin_elapsed(tokens)
		fullpath = check_get_fullpath(tokens)
		bytes_transfered = check_get_bytes_transfered(tokens)
		filesize = check_get_filesize(tokens)

		if bytes_transfered >= 0 and fullpath.startswith("/home"):
			if tokens[4] == 'sys_write':
				return "\t".join(["write", begin_elapsed, fullpath, str(bytes_transfered), filesize])
			elif tokens[4] == 'sys_read':
				return "\t".join(["read", begin_elapsed, fullpath, str(bytes_transfered)])
	
	return None

# line sample from the original trace: 
#	uid pid tid exec_name sys_unlink begin-elapsed cwd pathname return
#	1159 2364 32311 (eclipse) sys_unlink 1318539134533662-8118 /home/thiagoepdc/ /local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png 0
# line transformed by clean_unlink
#	unlink	begin-elapsed	fullpath	
#	unlink	1318539134533662-8118	/local/thiagoepdc/workspace_beefs/.metadata/.plugins/org.eclipse.jdt.ui/jdt-images/1.png	
def clean_unlink(tokens):
	if len(tokens) != 9:
		raise Exception("missing tokens in sys_unlink")

	fullpath = remove_basedir_if_present(" ".join(tokens[6:len(tokens)-2]))

	if fullpath.startswith("/home"):
		return "\t".join(['unlink', tokens[5], fullpath])
	else:
		return None

def main():
	global fdpid_to_fullpath
	global fullpath_to_filetype
	global fullpath_to_filesize
	
	if len(sys.argv) != 3:
		print "Usage: " + sys.argv[0] + " <input maps file> <output maps file>"
		sys.exit(1)
	
	serialized_maps = open(sys.argv[1], "r")
	map_of_maps = deserialize_maps(serialized_maps)
	
	fdpid_to_fullpath = map_of_maps['fdpid_to_fullpath']
	fullpath_to_filetype = map_of_maps['fullpath_to_filetype']
	fullpath_to_filesize = map_of_maps['fullpath_to_filesize']
	
	for line in sys.stdin:
		tokens = line.split()
	
		try:
			clean_line = None
			
			if tokens[4] == 'sys_open':
				handle_sys_open(tokens)
			elif tokens[4] == 'do_filp_open':
				handle_do_filp_open(tokens)
			elif tokens[4] == 'sys_close':
				clean_line = clean_close(tokens)
			elif tokens[4] == 'sys_write' or tokens[4] == 'sys_read':
				clean_line = clean_read_write(tokens)
			elif tokens[4] == 'sys_unlink':
				clean_line = clean_unlink(tokens)
	
			if clean_line != None:
				print clean_line
		except Exception as e:
			sys.stderr.write("-> ".join(["ERROR", e.args[0], line]))
	
	map_of_maps = dict()
	map_of_maps['fdpid_to_fullpath'] = fdpid_to_fullpath
	map_of_maps['fullpath_to_filetype'] = fullpath_to_filetype
	map_of_maps['fullpath_to_filesize'] = fullpath_to_filesize
	
	serialize_maps(map_of_maps, sys.argv[2])
	
### Main ###
if __name__ == "__main__":
	main()
### End ###
