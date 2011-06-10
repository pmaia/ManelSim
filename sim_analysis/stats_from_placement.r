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


for (migration in list("true", "false")) {
	for (migrationprob in list(0.2, 0.5, 0.8)) { 

	    cobalance_dir   <- paste(migration, "/", migrationprob, "/", "co-balance", "/", sep="")
	    corandom_dir  <- paste(migration, "/", migrationprob, "/", "co-random", "/", sep="")

   	    cobalance_path <- paste(outputdir, "/", cobalance_dir, "/", "random", "/", machine,"-",migration, "-",migrationprob,"-","co-balance","-", "random.out.clean", sep="")
   	    corandom_path <- paste(outputdir, "/", corandom_dir, "/", "random", "/", machine,"-",migration, "-",migrationprob,"-","co-random","-", "random.out.clean", sep="")
	    
 	    cobalance_data <- preparedata(read.csv(cobalance_path, header=FALSE, sep='\t'))
            corandom_data  <- preparedata(read.csv(corandom_path, header=FALSE, sep='\t'))

	    if ( length(corandom_data[[1]]) == length(cobalance_data[[1]]) ) {
        	   cat(paste("\n", migration, "-", migrationprob, "\n"))
		   print(t.test(corandom_data[[1]], cobalance_data[[1]], conf.level = 0.9))
	    }
	    else {
		   cat(paste("\n", "diff sizes",migration, "-", migrationprob,"\n")) 
            }
	
            cat("\n---------------\n")
    }
}
