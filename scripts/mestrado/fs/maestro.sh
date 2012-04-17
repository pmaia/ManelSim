#!/bin/bash

traces_dir=$1
output_file=$2

traces_files=`ls $traces_dir | grep  "log"`

for trace_file in $traces_files 
do
	cat $traces_dir/$trace_file | ./filter_fs_trace.py >> $output_file
done
