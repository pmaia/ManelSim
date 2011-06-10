numfiles=$1
file1=$2
file2=$3
file3=$4
outputname=$5

R --slave --args $numfiles $file1 $file2 $file3 $outputname < plot_by_relocation.r
