#!/bin/bash

if [ $# -lt 4 ]
then 
    echo "resultdir machine nummachines outputdir"
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

resultdir=$1
machine=$2
nummachines=$3
outputdir=$4
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
					if [ ! -f $outputdir/$finaldir ]
					then
						mkdir -p $outputdir/$finaldir
					fi
# 					$machine-$placement-$nummachines-$highmigration-$migrationprob-$sample-$migration_delay$data_migration
					simulation_output_pattern=$r_delay-$machine-$placement-$nummachines-$highmigration-$migrationprob-*-$migration_delay$data_migration
					cp $resultdir/$simulation_output_pattern-* $outputdir/$finaldir/
					outpath=$outputdir/$finaldir/$r_delay-$machine-$placement-$nummachines-$highmigration-$migrationprob-$migration_delay$data_migration.out
					sh concatResults.sh  $outputdir/$finaldir/ $outpath
					python cleanResults.py < $outpath > $outpath.clean
				done
			done
		done
	done
done
