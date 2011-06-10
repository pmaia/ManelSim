# Arguments
# 0. trace file 
# 1. Data placement police [random, co-random, co-balance] 
# 2. number of machines 
# 3. homeless login [true, false] 
# 4. migration probability [0, 1) 
# 5. data migration [true, false]
# 6. replication delay secs

if [ ! $# -eq 7 ]
then
	echo "Usage: trace placement num_machines homeless_login migration_prob data_migration replication_delay"
	exit 1
fi

trace=$1
placement=$2
num_machines=$3
homeless_login=$4
migration_prob=$5
data_migration=$6
replication_delay=$7

java -server -Xmx1024m -Xms1024m -cp lib/*:classes/ ddg.emulator.SeerTraceMain $trace $placement $num_machines $homeless_login $migration_prob $data_migration $replication_delay
