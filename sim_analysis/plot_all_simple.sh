#!/bin/bash

if [ $# -lt 3 ]
then 
    echo "machine nummachines outputdir"
    exit 1
fi

# THE RESULTDIR MUST BE ORGANIZED USING THE FOLLOWING PATTERN:
# resultdir
#   -------- delay-A
#   -------- delay-B
#   -------- delay-C
#
# THE FILES INSIDE EACH CHILD DIRECTORY MUST USE THE FOLLOWING PATTERN:
# $machine-$placement-$nummachines-$highmigration-$migrationprob-$sample-$migration_delay$data_migration.$RANDOM.out
# e.g machine5-co-random-50-true-1-1-30true-9870.out

machine=$1
nummachines=$2
outputdir=$3
placement="co-random"

for r_delay in 30 60 120
do
	for highmigration in true
	do
		for migration_delay in 0
		do
			for data_migration in false
			do
				for migrationprob in 1
				do
					finaldir=$r_delay/$highmigration/$migration_delay/$data_migration/$migrationprob/$placement/

					outpath=$outputdir/$finaldir/$r_delay-$machine-$placement-$nummachines-$highmigration-$migrationprob-$migration_delay$data_migration.out.clean

					bash plot.sh $outpath
					#mv $outputdir/$finaldir/*png .
				done
			done
		done
	done
done
