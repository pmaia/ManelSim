#!/bin/bash

if [ ! $# -eq 2 ]
then
	echo "Usage:" $0 "configuration_file manelsim_path"
	exit 1
fi

#FIXME: after testing this, assume the layout manelsim_path/classes 
#	manelsim_path/lib

config_file=$1
manel_sim=$2

if [ ! -f $config_file ]
then
	echo "File:" $config_file "is not available"
	exit 1
fi

if [ ! -d $manel_sim ]
then
	echo "File:" $manel_sim "is not available"
	exit 1
fi

java -server -Xmx1024m -Xms1024m -cp ../../lib/*:$manel_sim/classes core.ManelSim $config_file
