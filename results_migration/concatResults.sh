dir=$1
out_file=$2

for file in `ls $dir | sort` ; 
do
   head -n 2 $dir/$file | tail -n 1 >> $out_file ; 
done
