#!/bin/bash

on_die() {
	for pid in $pids;
	do
		echo "Killing $pid..."
		kill -9 $pid
	done

	echo "Bye bye :("

	exit 0
}

trap 'on_die' EXIT 

cores_number=`cat /proc/cpuinfo | grep processor | wc -l`

cd fbench/java

for i in `seq 1 $cores_number`; 
do
	java fbench 1000000000 > /dev/null &
	pids="$pids $!"
done

while true;
do
	echo "Just waiting until someone kill me. While I'm alive I'll keep all cores in this machine busy."
done
