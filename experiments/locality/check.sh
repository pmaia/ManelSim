#!/bin/bash

# It check the number of ManelSim scheduler errors
sim_log_path=$1
if [ ! -f $sim_log_path ]
then
    echo "File:" $sim_log_path "not accessible"
    exit 1
fi

pattern="ERROR"

#not so big, streaming twice
total_errors=`grep $pattern $sim_log_path | wc -l`
outdated=`grep $pattern $sim_log_path | grep "outdated" | wc -l`

echo "Total errors simlog:" $total_errors
echo "Outdated errors simlog:" $outdated
