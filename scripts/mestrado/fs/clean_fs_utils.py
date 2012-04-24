
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

def remove_duplicated_parent(path_with_duplicated_parent):#this is not supposed to be bulletproof but I think it works for most of the cases
	if path_with_duplicated_parent.startswith("//"):
		return path_with_duplicated_parent[1:]
	else:
		matches = []
	
		s = 1 
		while s < len(path_with_duplicated_parent):
			i = s 
			while i < len(path_with_duplicated_parent):
				if path_with_duplicated_parent[i] == path_with_duplicated_parent[0]:
					idx = i
					s = i
					while idx < len(path_with_duplicated_parent) and \
							path_with_duplicated_parent[idx] == path_with_duplicated_parent[idx - i]:
						idx += 1
	
					if (idx - 1 - i) <= i:
						matches.append((i, idx - 1))
						break
				i += 1
			s += 1
	
		first_after_basepath = 0
		max_interval = 0
		for (start, end) in matches:
			if end - start > max_interval:
				max_interval = end - start
				first_after_basepath = start
	
		return path_with_duplicated_parent[first_after_basepath:]
