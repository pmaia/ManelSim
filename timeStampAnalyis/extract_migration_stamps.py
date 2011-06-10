import sys
import io

"it receives a column file with the simulation timestamps"
migration_delay_millis = long(sys.argv[1])

last_migration = 0 #as the trace starts from a high number, 0 is enough to the first sub be greater than a typical delay
for line in sys.stdin:
    if (int(line) - last_migration) >= migration_delay_millis:	
	last_migration = int(line)
	print last_migration
