# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 
Args <- commandArgs(); 
file=Args[4]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

foo	<-	data$V2 + 0
proball <- c()
for (i in seq(1:length(foo))) {
	local <- data$V1[[i]] + data$V5[[i]]
	total <- foo[[i]] + data$V6[[i]]
	proball[[i]] <-	ifelse((local == 0 || total == 0), 0, local/total)
}

png(paste(file , "_density_readAndWrite.png", sep=""))
plot(density(proball, na.rm = TRUE))

png(paste(file , "_hist_readAndWrite.png", sep=""))
plot(hist(proball))

cat("\n")
cat("median")
cat("\n")
cat(median(proball))
cat("\n")
cat("mean")
cat("\n")
cat(mean(proball))
