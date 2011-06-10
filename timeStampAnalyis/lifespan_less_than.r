# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

#file1 lifespan_freq.txt
#file2 lifespan.txt
#R --slave --args lifespan.txt < lifespan.r

Args <- commandArgs(); 
file=Args[4]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

lifespan <- data$V4
hour_millis <- 1000 * 60 * 60.0

result <- c()
hours = c(1, 2, 4, 8, 12, 24)

for (i in c(1:length(hours))) {#hours
	result[i] = length(lifespan[lifespan < (hours[i] * hour_millis)] )
}

png("lifespan_less.png")

barplot((1 - result/length(lifespan)), names.arg=c("1", "2", "4", "8", "12", "24"), xlab="delay", ylab="lifespan >= delay", ylim=c(0, 0.05))
title("Lifespan e delay de migração")