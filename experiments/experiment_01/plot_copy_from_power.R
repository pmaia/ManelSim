library("ggplot2")

power_and_cpu_use_data <- read.table("utilization_power_freq_tabbed.txt", header=TRUE, sep="\t", dec=".")
copy_from_times <- read.table("copyFromTimes_1338331573348_Rfriendly")
copy_from_times <- copy_from_times / 1000

copy_from_is_running <- function(row) {
	for(i in 1:length(copy_from_times$V1)){
		if(row[1] >= copy_from_times$V1[i] & row[1] < copy_from_times$V2[i]) {
			return ("running")
		}
	}
	return ("not_running")
}
#http://stackoverflow.com/questions/7883154/how-do-i-fill-a-geom-area-plot-using-ggplot

power_and_cpu_use_data$category <- apply(power_and_cpu_use_data, 1, copy_from_is_running)

cat.rle = rle(power_and_cpu_use_data$category == "running")
power_and_cpu_use_data$group = rep.int(1:length(cat.rle$lengths), times=cat.rle$lengths)

png("time_vs_power_vs_copyFrom.png")

p <- ggplot(data=power_and_cpu_use_data, aes(y=power.W.,x=timestamp, fill=category, group=group)) + geom_area() 
p <- p + coord_cartesian(ylim=c(0,40)) 
p <- p + xlab("Time") + ylab("Power(W)")
p <- p + scale_fill_hue("Copy from:", breaks=c("running", "not_running"), labels=c("Running", "Not running")) + scale_x_datetime()

p

dev.off()

