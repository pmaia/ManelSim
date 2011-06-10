numfiles=$1
file1=$2
file2=$3

R --slave --args $numfiles $file1 $file2 < plot_many.r
