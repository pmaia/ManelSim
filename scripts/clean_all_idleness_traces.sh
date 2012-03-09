#!/bin/bash

traces_folder=$1
results_folder=$2

for trace in `ls $traces_folder`
do
	result_name=`echo $trace | cut -d "_" -f1`
	cat $traces_folder/$trace | ./clean_idleness_trace.py > $results_folder/$result_name
done
