Args <- commandArgs();

resultdir=Args[4]
machine=Args[5]
nummachines=Args[6]
fullmachines=Args[7]
outputdir=Args[8]

preparedata<-function (dataset) {
  # prop_byte_read prop_ops_read prop_byte_write prop_ops_write
	list(dataset$V1/dataset$V2, dataset$V3/dataset$V4, dataset$V5/dataset$V6, dataset$V7/dataset$V8)
}

numsamples<-function(data) {
	mean	<-	mean(data)
        cat("\nMean ", mean)
	sdev	<- 	sd(data)
        cat("\nsdev ", sdev)
	n 	<-	( (100 * 1.96 * sdev) / ( 5 * mean) ) ^ 2
}


# stats from relocation algorithm
for (migration in list("false")) {
	for (migrationprob in list(0.2, 0.5, 0.8)) { 
        	for (placement in list("random", "co-balance", "co-random")) {

	    	    finaldir    <- paste(migration, "/", migrationprob, "/", placement, "/", sep="")                                             
	   	    random_path <- paste(outputdir, "/", finaldir, "/", "random", "/", machine,"-",migration, "-",migrationprob,"-",placement,"-", "random.out.clean", sep="")
	    	    ws_path     <- paste(outputdir, "/", finaldir, "/", "workingset", "/",machine,"-",migration,"-",migrationprob,"-",placement,"-","workingset.out.clean", sep="")
	    
     		    random_data <- preparedata(read.csv(random_path, header=FALSE, sep='\t'))
            	    ws_data 	<- preparedata(read.csv(ws_path, header=FALSE, sep='\t'))

		    if ( length(random_data[[1]]) == length(ws_data[[1]]) ) {
                            cat(paste("\n", migration, "-", migrationprob, "-", placement, "\n"))
			    print(t.test(random_data[[1]], ws_data[[1]], conf.level = 0.50))
		    }
		    else {
		    	cat(paste("\n", "diff sizes",migration, "-", migrationprob, "-", placement, "\n")) 
                    }
	

 		   cat("\nnumsample random == ",numsamples(random_data[[1]]))
		   cat("\nnumsample LRU == ",numsamples(ws_data[[1]]))
                   cat("\n---------------\n")
	}
    }
}

# stats from placement
for (migration in list("false")) {
    for (migrationprob in list(0.2, 0.5, 0.8)) { 
	# fixing random relocation
	finaldir_co_balance    <- paste(migration, "/", migrationprob, "/", "co-balance", "/", sep="")
	finaldir_co_random     <- paste(migration, "/", migrationprob, "/", "co-random", "/", sep="")
	co_balance_path  <- paste(outputdir, "/", finaldir_co_balance, "/", "random", "/", machine,"-",migration, "-",migrationprob,"-", "co-balance", "-", "random.out.clean", sep="")
	co_random_path   <- paste(outputdir, "/", finaldir_co_random, "/", "random", "/",machine,"-",migration,"-",migrationprob,"-", "co-random","-","random.out.clean", sep="")
	
	co_balance_data <- preparedata(read.csv(co_balance_path, header=FALSE, sep='\t'))
	co_random_data  <- preparedata(read.csv(co_random_path, header=FALSE, sep='\t'))

	if ( length(co_balance_data[[1]]) == length(co_random_data[[1]]) ) {
	    cat(paste("\n", migration, "-", migrationprob, "\n"))
	    print(t.test(co_balance_data[[1]], co_random_data[[1]], conf.level = 0.90))
	}
	else {
	    cat(paste("\n", "diff sizes",migration, "-", migrationprob,"\n")) 
	}
	cat("\n---------------\n")
  }
}
