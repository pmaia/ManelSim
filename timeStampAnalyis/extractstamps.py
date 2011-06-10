import sys
import io

def parsetime(timeasstr):
   splited = timeasstr.split(".")
   firsttoken_asmilli = long(splited[0]) * 1000
   secondtoken_asmilli = long(splited[1][0:2])
   return firsttoken_asmilli + secondtoken_asmilli

for line in sys.stdin:
    if line.startswith("read") or line.startswith("write") or line.startswith("close"): 
	print parsetime(line.split()[1])
    elif line.startswith("open"):
	print parsetime(line.split()[2])
