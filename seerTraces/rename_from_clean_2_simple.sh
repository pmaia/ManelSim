directory=$1

for file in $directory/*
do
  filename=${file%.*clean}
  cat $file > "$filename"
  rm -f $file                  
done
