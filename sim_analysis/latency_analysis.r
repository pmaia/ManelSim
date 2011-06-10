# -*- coding: utf-8 -*-
#R --slave --args lifespan.txt < lifespan.r

# filename op_type length waslocal time through stamp
Args <- commandArgs();

for (file in list.files(pattern=".latency")) {
	data<-read.csv(file, header=FALSE, sep='\t')
	print(file)
	print(summary(data$V6))
	print("------------")
}


