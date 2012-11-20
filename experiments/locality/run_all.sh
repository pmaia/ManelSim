#!/bin/bash

# It run a parameter sweep to a collection of traces

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then 
    echo "Usage:" $0 "trace_dir" "out_dir"
    exit 1
fi

trace_dir=$1
if [ ! -d $trace_dir ]
then
    echo "trace_dir:" $trace_dir "is not accessible"
    exit 1
fi

out_dir=$2
if [ ! -d $out_dir ]
then
    echo "out_dir:" $out_dir "is not accessible"
    exit 1
fi

function create_config {
    local trace_path=$1
    local config_path=$2
    local new_config_path=$3

    local replacement="fs_trace_file="$trace_path

    #as the config_path have slashes "/" I'm using "," as sed delimiter
    #assuming we do not have "," char in our strings
    sed 's,fs_trace_file=.*,'"$replacement"',' $config_path > $new_config_path
}

function machine {
    local path=$1
    local machine=`basename $path | cut -d"." -f1`
    echo $machine
}

#assuming all file withing $trace_dir are trace files
#also, trace files follow a machine.* patttern
for file in `find $trace_dir -type f`
do
    mac=`machine $file`
    create_config $file conf/run.conf $out_dir/$mac.run.conf
    rm ddg.log
    bash run.sh $out_dir/$mac.run.conf ../../target 2> $mac.err
    mv ddg.log $out_dir/$mac.ddg.log
    mv *err $out_dir
done
