#!/usr/bin/python

'''
Raw data sample (from line 6 to 17):

1338330234
Linux 3.0.0-20-generic-pae (patrick-laptop)     05/29/2012      _i686_  (2 CPU)
 
07:23:54 PM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest   %idle
07:23:54 PM  all    3.83    1.39    1.88    6.87    0.00    0.06    0.00    0.00   85.97
800000
present:                 yes
capacity state:          ok
charging state:          discharging
present rate:            1 mA
remaining capacity:      1082 mAh
present voltage:         12361 mV 
'''

import sys

def main():
	print "timestamp\tpower(W)\tfreq\t%usr\t%nice\t%sys\t%iowait\t%irq\t%soft\t%steal\t%guest\t%idle"

	absolute_line_number = 0
	for line in sys.stdin:
		absolute_line_number += 1
		relative_line_number = absolute_line_number % 12

		if relative_line_number == 1:
			timestamp = line.strip()
		elif relative_line_number == 5:
			cpu_usage = '\t'.join(line.split()[3:])
		elif relative_line_number == 6:
			cpu_frequency = float(line.strip()) / 1000000
		elif relative_line_number == 10:
			discharging_rate = float(line.split()[2]) / 1000
		elif relative_line_number == 0: 
			voltage = (float(line.split()[2]) / 1000)
			power = voltage * discharging_rate
			print '\t'.join([timestamp, str(power), str(cpu_frequency), cpu_usage])
		
if __name__ == "__main__":
	main()

