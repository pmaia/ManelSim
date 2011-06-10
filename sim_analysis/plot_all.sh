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
for highmigration in false ;
do 
    for migrationprob in 0.2 0.5 0.8 ;
    do 	    
        for placement in random co-balance co-random ;
        do 
	    finaldir=$highmigration/$migrationprob/$placement/                                             
	    outpath1=$outputdir/$finaldir/random/$machine-$highmigration-$migrationprob-$placement-random.out.clean
	    outpath2=$outputdir/$finaldir/workingset/$machine-$highmigration-$migrationprob-$placement-workingset.out.clean
	    # plot assumes that the first is random and the second is working set
	    sh plot_many.sh 2 $outpath1 $outpath2
            cp *png $outputdir/$finaldir/
	    sh boxplot_many.sh 2 $outpath1 $outpath2 prob-$migrationprob
	done
    done
done

echo "++++++++++++++++++"

for highmigration in false ;
do 
    for relocation in random workingset ;
    do    
        for placement in random co-balance co-random ;
        do 
	    outpath1=$outputdir/$highmigration/"0.2"/$placement/$relocation/$machine-$highmigration-"0.2"-$placement-$relocation.out.clean
	    outpath2=$outputdir/$highmigration/"0.5"/$placement/$relocation/$machine-$highmigration-"0.5"-$placement-$relocation.out.clean
	    outpath3=$outputdir/$highmigration/"0.8"/$placement/$relocation/$machine-$highmigration-"0.8"-$placement-$relocation.out.clean
	    
   	    # plot assumes the following sentence 0.2 0.5 0.8
            sh plot_by_relocation.sh 3 $outpath1 $outpath2 $outpath3 $highmigration-$relocation-$placement
	done
   done
done
