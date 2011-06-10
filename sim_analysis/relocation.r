Args <- commandArgs();

cobalance<-read.csv(Args[4], header=FALSE, sep='\t')# co-balance
corandom<-read.csv(Args[5], header=FALSE, sep='\t')# co-random

cobalance <- cobalance / 1024 / 1024
corandom  <- corandom / 1024 / 1024

cobalance_prim 	<- mean(cobalance$V1)
corandom_prim	<- mean(corandom$V1)

cobalance_sec 	<- mean(cobalance$V2)
corandom_sec	<- mean(corandom$V2)

data <- c(cobalance_prim, corandom_prim, cobalance_sec, corandom_sec)

png(paste(Args[6],".png", sep=""))
barplot(data, names.arg=c("cobalance_prim", "corandom_prim", "cobalance_sec", "corandom_sec"))

numsamples<-function(data) {
        mean    <-      mean(data)
        cat("\nMean ", mean)
        sdev    <-      sd(data)
        cat("\nsdev ", sdev)
        n       <-      ( (100 * 1.96 * sdev) / ( 5 * mean) ) ^ 2
}

cat(paste("\n", Args[6], "\n"))
print(t.test(cobalance$V2, corandom$V2, conf.level = 0.90))

cat("\n")

cat("\nnumsample balacene == ",numsamples(cobalance$V2))
cat("\nnumsample LRU == ",numsamples(corandom$V2))
cat("\n---------------\n")
