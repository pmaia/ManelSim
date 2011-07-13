# -*- coding: iso-8859-1 -*-
import os
import sys

#files = ['machine01.concat', 'machine02.concat', 'machine03.concat', 'machine05.concat', 'machine06.concat', 'machine08.concat']
#files = ['machine07-part1.concat', 'machine07-part2.concat', 'machine09.concat', 'machine10.concat', 'machine11.concat', 'machine12.concat', 'machine13-part1.concat', 'machine13-part2.concat']
files = ['machine05.concat']

for filename in files:
   print filename
   simple_file = filename + ".simple"
   timeconverted = simple_file + ".timemilli"
   clean_file = timeconverted + ".clean"
   os.system('python simplifyTraceFile.py < ' + filename    + ' > ' + simple_file)
   os.system('python format_timestamps.py < ' + simple_file + ' > ' + timeconverted)
   os.system('python cleanDirectories.py  < ' + timeconverted + ' > ' + clean_file)
   os.system('python cleanGarbage.py ' + clean_file + ' true' + ' false')

