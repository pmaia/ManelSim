#!/usr/bin/python

import sys
import re

for line in sys.stdin:
	if not re.search("S_IFIFO|S_IFSOCK|S_IFBLK|S_IFCHR", line) and re.search("sys_(open|close|write|read|unlink)|do_filp_open", line):
		print line.strip()


