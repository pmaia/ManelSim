
# EXECUTAR ESTE SCRIPT NO DIRETORIO QUE CONTEM OS .OU# EXECUTAR ESTE SCRIPT NO DIRETORIO QUE CONTEM OS .OUT
outputdir=$1


for highmigration in false ;
do 
    for migrationprob in 0.2 0.5 0.8 ;
    do 	    
        for placement in random co-balance co-random ;
        do
	    for relocation in random workingset ;
            do 
	    	for file in machine5-$placement-$relocation-50-$highmigration-$migrationprob-*out ;
		do 
		    primary=`head -n3 $file | tail -n1 | cut -d ' ' -f 2`
		    sec=`head -n4 $file | tail -n1 | cut -d ' ' -f 2`
		    mkdir -p $outputdir/$highmigration/$placement/$relocation/$migrationprob
		    echo -e $primary"\t"$sec > $outputdir/$highmigration/$placement/$relocation/$migrationprob/$file 
		done
	   done
	done
    done
done

for highmigration in false ;
do 
    for migrationprob in 0.2 0.5 0.8 ;
    do 	    
        for placement in random co-balance co-random ;
        do
	    for relocation in random workingset ;
            do 
		sh concatResults.sh $outputdir/$highmigration/$placement/$relocation/$migrationprob $outputdir/$highmigration-$placement-$relocation-$migrationprob.reloc		
	    done
	done
    done
done
