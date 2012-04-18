
def deserialize_maps(serialized_maps):
	maps = dict()
	
	for line in serialized_maps:
		if line.startswith("#"):
			map_name = line.split()[1]
			maps[map_name] = dict()
		else:
			tokens = line.split()
			maps[map_name][tokens[0]] = tokens[1]
	
	return maps

def serialize_maps(map_of_maps, output_file_path):
	output_file = open(output_file_path, "w")

	for k, v in map_of_maps.iteritems():
		output_file.write('#\t' + k + '\n')
		for k1, v1 in v.iteritems():
			output_file.write(k1 + '\t' + v1 + '\n')
