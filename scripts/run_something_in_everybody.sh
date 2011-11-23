#!/bin/bash

# uses all files in a folder as input for the given script and stores the output in files in output_dir

folder=$1
script=$2
output_dir=$3

for file in `ls $folder`
do
	echo "running $script in $file"
	cat $folder/$file | $script > $output_dir/$file
done
