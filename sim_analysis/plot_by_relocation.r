# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

preparedata<-function (dataset) {
  # prop_byte_read prop_ops_read prop_byte_write prop_ops_write
  list(dataset$V1/dataset$V2, dataset$V3/dataset$V4, dataset$V5/dataset$V6, dataset$V7/dataset$V8)
}

Args <- commandArgs();
numfiles=Args[4]

outputname <- Args[4 + as.numeric(numfiles) + 1]

files<-list()
datalist<-list()

for (i in seq(1:numfiles) ) {
  file=Args[4 + i]
  files[[i]]<-file
  foo<-preparedata(read.csv(file, header=FALSE, sep='\t'))
  datalist[[i]]<-foo
}

parsepath<-function(path) {
  	tmppath<-unlist(strsplit(path, "/"))
  	tmppath<-tmppath[[length(tmppath)]]
}
 
png(paste(outputname, ".png", sep=""))

datatmpf1 <- datalist[[1]]
datatmpf2 <- datalist[[2]]
datatmpf3 <- datalist[[3]]
 
minimum <- min( min(datatmpf1[[1]]), min(datatmpf2[[1]]), min(datatmpf3[[1]]))
maximum <- max( max(datatmpf1[[1]]), max(datatmpf2[[1]]), max(datatmpf3[[1]]))

plot(main="", ecdf(datatmpf1[[1]]),  col.hor="red",   col.vert="red",   pch=1, xlim=c(minimum, maximum))
lines(main="", ecdf(datatmpf2[[1]]), col.hor="green", col.vert="green", pch=2, xlim=c(minimum, maximum))
lines(main="", ecdf(datatmpf3[[1]]), col.hor="blue",  col.vert="blue",  pch=3, xlim=c(minimum, maximum))

legend(x="right", c("p = 0.2", "p = 0.5", "p = 0.8"), col=c("red", "green", "blue"), pch=c(1, 2, 3))  
