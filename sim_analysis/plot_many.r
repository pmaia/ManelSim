# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

preparedata<-function (dataset) {
  # prop_byte_read prop_ops_read prop_byte_write prop_ops_write
  list(dataset$V1/dataset$V2, dataset$V3/dataset$V4, dataset$V5/dataset$V6, dataset$V7/dataset$V8)
}

Args <- commandArgs();
numfiles=Args[4]

files<-list()
datalist<-list()

for (i in seq(1:numfiles) ) {
  file=Args[4 + i]
  files[[i]]<-file
  foo<-preparedata(read.csv(file, header=FALSE, sep='\t'))
  datalist[[i]]<-foo
}

graphs<-list("prop_byte_read", "prop_ops_read", "prop_byte_write", "prop_ops_write")

parsepath<-function(path) {
  	tmppath<-unlist(strsplit(path, "/"))
  	tmppath<-tmppath[[length(tmppath)]]
}

for (i in seq(1:length(graphs))) {
  
  filename1<-parsepath(files[[1]])
  filename2<-parsepath(files[[2]])
  
  listnames<-list(filename1, filename2)
  
  ploted<-FALSE
  graphtmp<-graphs[[i]]
  png(paste(graphtmp, ".png", sep=""))
  for (data in datalist) {
    datatmp<-data[[i]]             
    if ( ! ploted ) {        
      if (i == 1) {
	cat(paste(listnames[[1]], graphtmp))
      }
      ploted<-TRUE      
      plot(main="", ecdf(datatmp), col.hor="red", col.vert="red", pch=1, xlim=c(min(datatmp),max(datatmp)))
    } else {
      if (i == 1) {
	cat(paste(listnames[[2]], graphtmp))
      }
      lines(main="", ecdf(datatmp), col.hor="green", col.vert="green", pch=2, xlim=c(min(datatmp),max(datatmp)))
    }
  }
  legend(x="left", c("random", "workingset"), col=c("red", "green"), pch=c(1, 2))  
  dev.off()
}
