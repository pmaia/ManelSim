import os
import sys

dir = sys.argv[1]

for dirpath, dirnames, filenames in os.walk(dir):
   for file in filenames:
      if not file.endswith('txt') and not file.endswith('TXT') and not file.endswith('gz'):
	 abspathfile = os.path.abspath(dirpath + '/' + file)
         print abspathfile
         os.remove(abspathfile)
