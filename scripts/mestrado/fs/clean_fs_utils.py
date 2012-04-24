
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

def remove_duplicated_parent(path_with_duplicated_parent):
	words = path_with_duplicated_parent[1:].split("/")
	matches = []

	for s in xrange(1, len(words)):
		for i in xrange(s, len(words)):
			if words[i] == words[0]:
				idx = i 
				while idx < len(words) and words[idx] == words[idx - i]:
					idx += 1

				if (idx - i) <= i:
					matches.append((i, idx - 1))
					break

	interval = None
	max_interval = 0
	for (start, end) in matches:
		if end - start >= max_interval:
			max_interval = end - start
			interval = (start, end)
	
	fullpath = ""
	if interval != None:
		start, end = interval
		for x in xrange(start, len(words)):
			fullpath += "/" + words[x]
	else:
		fullpath = path_with_duplicated_parent
	
	return fullpath
