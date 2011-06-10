if [ $# -lt 5 ]
then 
    echo "resultdir machine nummachines fullmachines outputdir"
    exit 1
fi

resultdir=$1
machine=$2
nummachines=$3
fullmachines=$4
outputdir=$5

R --slave --args $resultdir $machine $nummachines $fullmachines $outputdir < stats_from_factors.r
