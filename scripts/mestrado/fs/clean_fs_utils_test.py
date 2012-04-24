import unittest
from clean_fs_utils import *

class TestTraceWalk(unittest.TestCase):
	
	def test_deserialize_maps(self):
		line = [	"# fdpid_to_fullpath",
				"1-2	/home/test",
				"# fullpath_to_filetype",
				"/home/teste	1",
				"/home/teste/coruja	25",
				"# fullpath_to_filesize",
				"/home/teste	13"
			]
		
		maps = deserialize_maps(line)

		self.assertEquals(len(maps), 3)

		self.assertNotEqual(maps['fdpid_to_fullpath'], None)
		self.assertNotEquals(maps['fullpath_to_filetype'], None)
		self.assertNotEquals(maps['fullpath_to_filesize'], None)

		self.assertEquals(len(maps['fdpid_to_fullpath']), 1)
		self.assertEquals(len(maps['fullpath_to_filetype']), 2)
		self.assertEquals(len(maps['fullpath_to_filesize']), 1)

		self.assertEquals(maps['fdpid_to_fullpath']['1-2'], "/home/test")
		self.assertEquals(maps['fullpath_to_filetype']['/home/teste'], '1')
		self.assertEquals(maps['fullpath_to_filetype']['/home/teste/coruja'], '25')
		self.assertEquals(maps['fullpath_to_filesize']['/home/teste'], '13')

	def test_serialize_maps(self):
		one_map = dict()
		one_map['one_key'] = 'one_value'

		two_map = dict()
		two_map['another_one_key'] = 'another_one_value'

		map_of_maps = dict()
		map_of_maps['one_map'] = one_map
		map_of_maps['two_map'] = two_map

		serialize_maps(map_of_maps, '/tmp/serialized_map')

		serialized_map = open('/tmp/serialized_map', 'r')
	
		line = serialized_map.readline()
		self.assertEquals(line, '#\tone_map\n')
		line = serialized_map.readline()
		self.assertEquals(line, 'one_key\tone_value\n')
		line = serialized_map.readline()
		self.assertEquals(line, '#\ttwo_map\n')
		line = serialized_map.readline()
		self.assertEquals(line, 'another_one_key\tanother_one_value\n')

	def test_remove_duplicated_parent(self):
		self.assertEquals(remove_duplicated_parent("/home/user/home/user/file"), "/home/user/file")
		self.assertEquals(remove_duplicated_parent("/home/user/file"), "/home/user/file" )
		self.assertEquals(remove_duplicated_parent("/home/user with blank/file"), "/home/user with blank/file")
		self.assertEquals(remove_duplicated_parent("/a/b/a/b/a/b/a/b/a/b/c"), "/a/b/a/b/c")
		self.assertEquals(remove_duplicated_parent("//a/a/file"), "//a/a/file")

if __name__ == "__main__":
	unittest.main()
