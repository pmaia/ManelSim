if [ $# -ne 8 ]
then
    echo "I need 8 parameters"
    exit 1
fi

numfiles=$1
file1=$2
file2=$3
file3=$4
file4=$5
file5=$6
file6=$7
outputname=$8

R --slave --args $numfiles $file1 $file2 $file3 $file4 $file5 $file6 $outputname < plot_by_login_hours.r
