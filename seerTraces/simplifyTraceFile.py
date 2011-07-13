# -*- coding: iso-8859-1 -*-
import sys
import io

init_header = ''

# init 960228927.143501
# open /usr/X11R6/lib/X11/fonts/misc/fonts.dir 960254165.543422 5
# read 960254162.929275 3 4096
# write 960254662.240004 3 1137
# close 960254662.240045 3
# 31152 UID 57541 PID 971 /bin/tcsh A 960254662.397974 dup(0) = 1
# 1997364 UID 12196 PID 1676 /usr/bin/netscape A 960353061.514438 dup2(3, 1) = 1


for line in sys.stdin:
	splited = line.split()
	if splited[1] == 'UID':
		op = splited[8]
		time = splited[7]
		if op.startswith('open('):
			f_name = op.split('"')[1]
			fd = line.split('=')[1].split()[0]
			if not fd == '-1':
				open_str = "\t".join(['open', f_name, time, fd])
				print open_str
		elif op.startswith('read('):
			fd = op.split('(')[1].split(',')[0]
			read_bytes = line.split('=')[1].split()[0]
			read_str = "\t".join(['read', time, fd, read_bytes])
			print read_str			
		elif op.startswith('write('):
			fd = op.split('(')[1].split(',')[0]
			write_bytes = line.split('=')[1].split()[0]
			write_str = '\t'.join(['write', time, fd, write_bytes])
			print write_str
		elif op.startswith('close('):
			fd = op.split('(')[1].split(',')[0]
			close_str = "\t".join(['close', time, fd])
			print close_str
#		elif op.startswith('dup('):#dup(2) = 3
#			fd0 = op.split('(')[1][0]
#			fd1 = line.split('=')[1].split()[0]
#			dup_str = '\t'.join(['dup', time, fd0, fd1, pid])
#			simple_trace.writelines('\n' + dup_str)
#		elif op.startswith("dup2"):#dup2(3, 0) = 0
#			fd0 = op.split('(')[1][0]
#			fd1 = line.split('=')[1].split()[0]
#			dup2_str = "\t".join(['dup2', time, fd0, fd1, pid])
#			simple_trace.writelines('\n' + dup2_str)