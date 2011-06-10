import os
import sys

dir = sys.argv[1]

for dirpath, dirnames, filenames in os.walk(dir):
   for file in filenames:
      if file.endswith('txt'):
         abspathfile = os.path.abspath(dirpath + '/' + file)
         print abspathfile
         os.system('python simplifyTraceFile.py ' + abspathfile)
         os.system('python cleanDirectories.py ' + abspathfile + '.simple')
         os.system('python cleanGarbage.py ' + abspathfile + '.simple.clean' + ' true' + ' false')

outdir = dir + '-simple'
os.system('mkdir ' + outdir)
os.system('mv ' + dir + '/*.simple.* ' + outdir)
