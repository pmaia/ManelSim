#!/usr/bin/python

'''
1339004869
Linux 3.0.0-20-generic-pae (patrick-laptop) 	06/06/2012 	_i686_	(2 CPU)

02:47:49 PM  CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest   %idle
02:47:50 PM  all    0.50    0.00    1.50    0.00    0.00    0.00    0.00    0.00   98.00
Average:     all    0.50    0.00    1.50    0.00    0.00    0.00    0.00    0.00   98.00
800000
present:                 yes
capacity state:          ok
charging state:          discharging
present rate:            1 mA
remaining capacity:      4400 mAh
present voltage:         12465 mV
'''

import sys

def main():
	print "timestamp\tpower(W)\tfreq\t%usr\t%nice\t%sys\t%iowait\t%irq\t%soft\t%steal\t%guest\t%idle"

	absolute_line_number = 0
	for line in sys.stdin:
		absolute_line_number += 1
		relative_line_number = absolute_line_number % 13

		if relative_line_number == 1:
			timestamp = line.strip()
		elif relative_line_number == 6:
			cpu_usage = '\t'.join(line.split()[2:])
		elif relative_line_number == 7:
			cpu_frequency = float(line.strip()) / 1000000
		elif relative_line_number == 11:
			discharging_rate = float(line.split()[2]) / 1000
		elif relative_line_number == 0: 
			voltage = (float(line.split()[2]) / 1000)
			power = voltage * discharging_rate
			print '\t'.join([timestamp, str(power), str(cpu_frequency), cpu_usage])
		
if __name__ == "__main__":
	main()

