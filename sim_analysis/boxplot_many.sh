numfiles=$1
file1=$2
file2=$3
outputname=$4

R --slave --args $numfiles $file1 $file2 $outputname < boxplot_many.r
