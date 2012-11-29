#!/bin/bash

# It run a parameter sweep to a collection of traces

EXPECTED_ARGS=3

if [ $# -ne $EXPECTED_ARGS ]
then 
    echo "Usage:" $0 "trace_dir" "out_dir" "num_samples"
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

#TODO: validate this value
samples=$3

function create_config {
    local trace_path=$1
    local config_path=$2
    local new_config_path=$3
    local pMigration=$4
    local migrationPolice=$5

    local replacement="fs_trace_file="$trace_path

    local new_prob="user_migration_probability="$pMigration
    local new_police="user_migration_algorithm="$migrationPolice

    #as the config_path have slashes "/" I'm using "," as sed delimiter
    #assuming we do not have "," char in our strings
    sed -e 's,fs_trace_file=.*,'"$replacement"',' -e 's/user_migration_algorithm=.*/'"$new_police"'/' -e 's/user_migration_probability=.*/'"$new_prob"'/' $config_path > $new_config_path
}

function machine {
    local path=$1
    local machine=`basename $path | cut -d"." -f1`
    echo $machine
}

#assuming all file withing $trace_dir are trace files
#also, trace files follow a machine.* patttern
for prob in "0.2" "0.5" "0.8"
do 
    for police in "sweet_home" "homeless"
    do
        sim_out_dir=$out_dir/$prob/$police
        if [ ! -d $sim_out_dir ]
        then
            mkdir -p $sim_out_dir
        fi

        for file in `find $trace_dir -type f`
        do
            for sample in `seq $samples`
            do 
                mac=`machine $file`
                create_config $file conf/run.conf $sim_out_dir/$mac.run.conf $prob $police
                rm ddg.log
                bash run.sh $sim_out_dir/$mac.run.conf ../../target 1> $mac.$sample.out 2> $mac.$sample.err
                mv ddg.log $sim_out_dir/$mac.$sample.ddg.log
                mv $mac.$sample.out $mac.$sample.err $sim_out_dir/
            done
        done
    done
done
