# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

# Receives two *out.clean files
Args <- commandArgs(); 

numfiles=Args[4]

files<-list()
datalist<-list()

allops<-function(data) {
        total_local <- data$V1 + data$V5
        total   <-      data$V2 + data$V6
        prob_allbytes<- total_local/total
}

for (i in seq(1:numfiles) ) {
  file=Args[4 + i]
  files[[i]]<-file
  foo<-read.csv(file, header=FALSE, sep='\t')
  cat(file)
  cat("\n")
  cat(summary(allops(foo)))
  cat("\n")
}

#boxplot(datalist)

#for (data in datalist) {
   
   #boxplot()	
   #if ( ! ploted ) {
   #  ploted<-TRUE
     #plot(main="", ecdf(datatmp), pch=pch_i, xlim=c(0.95, 1))
    # plot(main="", ecdf(datatmp), pch=pch_i, xlim=c(0.95, 1))
   #} else {
#     lines(main="", ecdf(datatmp),pch=pch_i, xlim=c(0.95, 1))
   #}
   #pch_i <- pch_i + 1	
#}

#legend(x="bottom", c("random", "working set"), col=c("red", "green"), pch=c(1, 2))
dev.off()
