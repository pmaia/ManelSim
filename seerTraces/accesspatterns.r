# test.R
# Parse and use command line arguments
# Invoke % R --slave --args 100 < test.R 
Args <- commandArgs(); 
file=Args[4]

x<-read.csv(file, header=FALSE, sep=' ')

data<-x$V3

zero_data <- data[data <= 1]
nonzero_data <- data[data > 1]

cat(paste("num of entries->", length(data)))

cat(paste("\nnum of entries with duration less than one ->", length(zero_data)))
cat(paste("\npercentual of entries with duration less than one ->", length(zero_data) / length(data)))

access_data <-x$V2
oneaccess_data <- data[data == 1]

cat(paste("\nnum of entries with access equals to one ->", length(oneaccess_data)))

cat("\n")
