#!/bin/bash

if [ ! $# -eq 1 ]
then
	echo "Usage:" $0 "configuration_file"
	exit 1
fi

config_file=$1

if [ -f $config_file ]
then
	echo "File:" $config_file "is not available"
	exit 1
fi

java -server -Xmx1024m -Xms1024m -cp lib/*:classes/ core.ManelSim $config_file

