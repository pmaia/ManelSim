dir=$1
out_file=$2

for file in `ls $dir | sort` ; 
do
   echo $file 
   cat $dir/$file >> $out_file ; 
done
