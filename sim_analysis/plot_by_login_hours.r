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

for (i in c(1:numfiles) ) {
  file=Args[4 + i]
  files[[i]]<-file
  foo<-preparedata(read.csv(file, header=FALSE, sep='\t'))
  datalist[[i]]<-foo
}

minimum = min(unlist(datalist[[1]][1]))
maximum = max(unlist(datalist[[1]][1]))

for (i in c(2:numfiles) ) {
	minimum <- min(minimum, unlist(datalist[[i]][1]))
	maximum <- max(maximum, unlist(datalist[[i]][1]))
}

png(paste(outputname, ".png", sep=""))
plot(main="", ecdf(unlist(datalist[[1]][1])), pch=1, xlim=c(minimum, maximum))
for (i in c(2:numfiles) ) {
	lines(main="", ecdf(unlist(datalist[[i]][1])), pch=i, xlim=c(minimum, maximum))
}
legend(x="right", c("1", "2", "4", "8", "12", "24"), pch=c(1, 2, 3, 4, 5, 6))


png(paste(outputname, "-boxplot.png", sep=""))
boxplot(xlab="delay", unlist(datalist[[1]][1]), unlist(datalist[[2]][1]), unlist(datalist[[3]][1]), unlist(datalist[[4]][1]), unlist(datalist[[5]][1]), unlist(datalist[[6]][1]), names=c("1", "2", "4", "8", "12", "24"), outline=FALSE)