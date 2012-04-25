#!/bin/bash

# uses all files paths from stdin as input for the given script and stores the output in files in output_dir. 
# The name of the resulting files are appended with posfix

script=$1
output_dir=$2
posfix=$3

while read file
do
	echo "running $script in $file"
	out_file=`basename ${file}.${posfix}`
	cat $file | $script > $output_dir/$out_file
done
