import sys

file = open(sys.argv[1], 'r')

count = 0
last = -1

for line in file:
    time = int(line.split()[1])
    if (last == -1):
        last = time
    
    if time == 0:
        if not last == 0:
            print "non_zero\t", count
            count = 1
        else:
	    count += 1
    else:
	if last == 0:
	    print "zero\t", count
            count = 1
        else:
	    count += 1

    last = time

file.close()
