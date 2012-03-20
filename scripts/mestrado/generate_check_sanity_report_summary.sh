#!/bin/bash

#keys="back,gap,time is less,time is greater"

dir=$1

keys="back,gap,\"time is smaller\",\"time is bigger\""
IFS=","
for key in $keys
do
	find $dir -name "*.diagnosis" -exec echo "err_count=\`grep $key {} | wc -l\`; echo $key \$err_count >> ${dir}/\$(basename {}).summary" \; >> /tmp/script
done

bash /tmp/script
