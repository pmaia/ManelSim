#!/bin/bash

machines="abelhinha celacanto charroco cherne gupi jurupoca mulato ourico palmito pepino pimpim roncador sargento traira viola"

filtered_traces_dir=$1
cleaned_traces_dir=$2

for machine in $machines
do
	machine_filtered_traces_dir=$filtered_traces_dir/$machine-filtered
	input_maps_file=$cleaned_traces_dir/$machine-aux-maps-in
	output_maps_file=$cleaned_traces_dir/$machine-aux-maps-out

	if [ -d $machine_filtered_traces_dir -a -f $input_maps_file ]; then
		echo "Cleaning $machine"
		cat_args=`ls $machine_filtered_traces_dir | sort -n -t '.' -k 1,14 -k 3 | xargs -i echo $machine_filtered_traces_dir/{}`
		cat $cat_args | ./clean_fs_trace.py $input_maps_file $output_maps_file > $cleaned_traces_dir/$machine-cleaned
	else
		echo "Skipping $machine"
	fi
done
