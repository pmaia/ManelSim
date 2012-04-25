#!/bin/bash


folder=$1

for file in `ls $folder`
do
	echo "$file"
	secs=`head -n 1 $folder/$file | cut -f1`
	echo "first event " + `date -d "1970-01-01 UTC + $secs seconds"`

	secs=`tail -n 1 $folder/$file | cut -f1`
	echo "last event " + `date -d "1970-01-01 UTC + $secs seconds"`
	echo ""
done
