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

datatmpf1 <- datalist[[1]]
datatmpf2 <- datalist[[2]]

png(paste(outputname, "-boxplot.png", sep=""))

# first random , second workingset
boxplot(datatmpf1[[1]], datatmpf2[[1]], names=c("random" , "workingset") , outline=FALSE)