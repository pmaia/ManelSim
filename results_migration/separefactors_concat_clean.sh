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

for highmigration in true false ;
do 
    for migrationprob in 1 ;
    do 	    
        for placement in random co-balance co-random ;
        do 
            for relocation in random workingset ;
            do  
                finaldir=$highmigration/$migrationprob/$placement/$relocation
                
		if [ ! -f $outputdir/$finaldir ]
                then 
                   mkdir -p $outputdir/$finaldir
                fi 
		
		cp $resultdir/$machine-$placement-$relocation-$nummachines-$highmigration-$migrationprob-* $outputdir/$finaldir/
                
		outpath=$outputdir/$finaldir/$machine-$highmigration-$migrationprob-$placement-$relocation.out
                sh concatResults.sh  $outputdir/$finaldir/ $outpath
                python cleanResults.py $outpath
	    done
	done
    done
done
