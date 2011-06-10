dir=$1
out_file=$2

for file in `ls $dir | sort` ; 
do
	head -n 4 $dir/$file | tail -n 2 >> $out_file ;
done
