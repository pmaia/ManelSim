#!/bin/bash

if [ $# -lt 5 ]
then 
    echo "resultdir machine nummachines fullmachines outputdir"
    exit 1
fi

resultdir=$1
machine=$2
nummachines=$3
fullmachines=$4
outputdir=$5


# plotting
for highmigration in false true;
do 
    for relocation in random workingset ;
    do
	for migrationprob in 1 ;
    	do 	
            for placement in random co-balance co-random ;
            do 
	    	finaldir=$highmigration/$migrationprob/$placement/                                             
	    	outpath=$outputdir/$finaldir/$relocation/$machine-$highmigration-$migrationprob-$placement-$relocation.out.clean
		if [ -e $outpath ]
		then
		   echo ploting $outpath
		   R --slave --args $outpath < plot.r
		   cp $outputdir/$finaldir/$relocation/*png .		
		fi
	    done
	done
    done
done
