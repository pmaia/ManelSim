# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 

#file1 numops2samples.txt (mode 3 file_popularity.py)
#file2 file2ops.txt (mode 2 file_popularity.py)
#R --slave --args numops2samples.txt file2ops.txt < plot_filepopularity.r

Args <- commandArgs(); 
file=Args[4]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

png(paste(file , "_file_popularity.png", sep=""))
plot(data, xlab="num operacoes", ylab="numero arquivos")
title("Popularidade de Arquivos")

png(paste(file , "_file_popularity_log.png", sep=""))
plot(data, xlab="num operacoes", ylab="numero arquivos", log="xy")
title("Popularidade de Arquivos")

#png(paste(file , "_file_popularity_cdf_log.png", sep=""))
#plot(ecdf(data$V1), xlab="num operacoes", ylab="numero arquivos")
#title("Popularidade de Arquivos CDf")

file=Args[5]
data<-read.csv(file, header=FALSE, sep='\t', blank.lines.skip = TRUE)

ranking <- sort(data$V2, decreasing = TRUE)

png(paste(file , "_file_ranking.png", sep=""))
plot(ranking, ylab="num operacoes", xlab="Ranking popularidade dos arquivos", log="xy")
title("Ranking de Popularidade dos Arquivos")
