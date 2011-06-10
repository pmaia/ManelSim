for highmigration in false ;
do
    for migrationprob in 0.2 0.5 0.8 ;
    do
	for relocation in random workingset ;
        do	     
	    R --slave --args $highmigration-"co-balance"-$relocation-$migrationprob.reloc $highmigration-"co-random"-$relocation-$migrationprob.reloc $highmigration-$relocation-$migrationprob < relocation.r
        done
    done
done
