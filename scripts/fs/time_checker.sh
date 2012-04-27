#!/bin/bash

input_dir=$1
output_dir=$2

for file in `find $input_dir -type f`
do
	echo "Checking $file"
	cat $file | ./time_checker.py > $output_dir/`basename $file`
done
