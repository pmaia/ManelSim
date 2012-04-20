#!/bin/bash

machines="abelhinha&celacanto&charroco&cherne&gupi&jurupoca&mulato&ourico&palmito&pepino&pimpim&roncador&sargento&traira&viola"
maps="# fdpid_to_fullpath&# fullpath_to_filetype&# fullpath_to_filesize"

IFS='&'

output_dir=$1

for machine in $machines
do
	for map in $maps
	do
		echo $map >> $output_dir/$machine-aux-maps-in
	done
done

