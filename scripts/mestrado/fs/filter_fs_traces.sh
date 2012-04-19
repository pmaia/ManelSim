#!/bin/bash

machines="abelhinha celacanto charroco cherne gupi jurupoca mulato ourico palmito pepino pimpim roncador sargento traira viola"

traces_dir=$1
output_dir=$2

for machine in $machines
do
	machine_traces_dir=$traces_dir/$machine

	if [ -d $machine_traces_dir -a ! -d $output_dir/$machine-filtered ]; then
		echo "Filtering $machine"

		mkdir $output_dir/$machine-filtered

		traces_files=`ls $machine_traces_dir | grep  "log"`

		for trace_file in $traces_files 
		do
			cat $machine_traces_dir/$trace_file | ./filter_fs_trace.py > $output_dir/$machine-filtered/$trace_file.filtered
		done
	else
		echo "Skipping $machine"
	fi
done
