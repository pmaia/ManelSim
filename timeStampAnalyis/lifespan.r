# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

#file1 lifespan_freq.txt
#file2 lifespan.txt
#R --slave --args lifespan_freq.txt lifespan.txt < lifespan.r

Args <- commandArgs(); 
file=Args[4]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

png(paste(file , "_lifespan_pop.png", sep=""))
plot(data, xlab="lifespan milliseconds", ylab="numero arquivos")
title("Popularidade do lifespan")

png(paste(file , "_lifespan_pop_log.png", sep=""))
plot(data, xlab="lifespan", ylab="numero arquivos", log="xy")
title("Popularidade do lifespan")

file=Args[5]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

ranking <- sort(data$V4, decreasing = TRUE)

png(paste(file , "_lifespan_ranking.png", sep=""))
plot(ranking, ylab="num operacoes", xlab="Ranking popularidade do lifespan", log="xy")
