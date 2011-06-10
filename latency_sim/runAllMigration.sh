#!/bin/bash

# Arguments
# 0. trace file 
# 1. number of machines
# 2. numsamples
# 3. data migration [true, false]
# 4. replication_coherence_delay (secs)
# 5. migration_delay (secs)


if [ ! $# -eq 3 ] ; then
    echo "usage: tracefile nummachines numsamples"
    exit 1
fi

tracefile=$1
nummachines=$2
samples=$3

machine="machine5"
placement="co-random"
homeless="true"
migrationprob="1"
hours_between_login=1

#REMEMBER, WHEN DATA_MIGRATION IS FALSE THERE IS NO NEED TO MIGRATION_DELAY

for sample in `seq 1 ${samples}`
do
	for r_delay in 30
	do
		for migration_delay in 0
		do
			for data_migration in true
			do
				for hours in 1
				do
					if [ ! -f $hours ]
					then
						mkdir -p $hours
					fi
					rand=$RANDOM
					allargs=$r_delay-$machine-$placement-$nummachines-$homeless-$migrationprob-$sample-$migration_delay$data_migration
					outfile=$allargs-${rand}.out
					python experiment.py $tracefile $placement $nummachines $homeless $migrationprob $data_migration $r_delay $migration_delay $hours > $hours/$outfile 2> $hours/$outfile.latency
				done
			done
		done
	done
done
