# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 
Args <- commandArgs(); 
file=Args[4]
freq<-read.csv(file, header=FALSE, sep='\t')

prop_byte_read<-freq$V1/freq$V2
prop_ops_read<-freq$V3/freq$V4

prop_byte_write<-freq$V5/freq$V6
prop_ops_write<-freq$V7/freq$V8

summary(prop_byte_read)
summary(prop_ops_read)
summary(prop_byte_write)
summary(prop_ops_write)

plot(ecdf(prop_byte_read))
title("ecdf(prop_byte_read)")
png(paste(file, "_ecdf_prop_byte_read.png", sep=""))

plot(ecdf(prop_ops_read))
title("ecdf(prop_ops_read)")
png(paste(file , "_ecdf_prop_ops_read.png", sep=""))

plot(ecdf(prop_byte_write))
title("ecdf(prop_byte_write)")
png(paste(file , "_ecdf_prop_byte_write.png", sep=""))

plot(ecdf(prop_ops_write))
title("ecdf(prop_ops_write)")
png(paste(file , "_ecdf_prop_ops_write.png", sep=""))

plot(ecdf(prop_ops_write))
title("ecdf(prop_ops_write)")
png(paste(file , "_ecdf_prop_ops_write.png", sep=""))
dev.off()
