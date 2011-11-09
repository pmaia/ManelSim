#!/bin/bash

traces_dir=$1
output_file=$2

traces_files=`ls $traces_dir | grep -P "\\d{14}-\\w+.log.\\d+"`

for trace_file in $traces_files 
do
	cat $traces_dir/$trace_file | ./filter_fs_trace.sh >> $output_file
done
