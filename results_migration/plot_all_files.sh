#!/bin/bash

input=$1

# plotting
tail -n $((`cat ${input} | wc -l` - 2)) $input | awk 'BEGIN { OFS="\t" } {print $2,$3,$4,$5,$6,$7,$8,$9}' > $input.tmp
R --slave --args $input.tmp < plot_allfiles_on_trace.r
#rm $input.tmp
