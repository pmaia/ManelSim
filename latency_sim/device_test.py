# -*- coding: utf-8 -*-
import unittest
from device import *

class TestObjectCreation(unittest.TestCase):

	def test_equality(self):
		ds1 = Device(1)
		ds2 = Device(2)
		ds3 = Device(1)
		
		self.assertEquals(ds1, ds1)
		self.assertNotEquals(ds1, ds2)
		self.assertNotEquals(ds2, ds1)
		
		self.assertEquals(ds1, ds3)
		self.assertEquals(ds3, ds1)

	def test_create_file_with_negative_size(self):
		ds = Device(1)
		self.assertRaises(ValueError, ds.createfile, "filename", -1)

	def test_error_when_creating_file_twice(self):
		ds = Device(1)
		filename = "filename"
		ds.createfile(filename, 64)
		self.assertRaises(FileAlreadyExistError, ds.createfile, filename, 128)

	def test_invalid_length_on_write(self):
		ds = Device(1)
		ds.createfile("filename", 0)
		self.assertRaises(ValueError, ds.write, "filename", 0, -1)#negative length
		
	def test_invalid_length_on_read(self):
		ds = Device(1)
		ds.createfile("filename", 0)
		self.assertRaises(ValueError, ds.read, "filename", 0, -1)#negative length

	def test_invalid_offset_on_write(self):
		ds = Device(1)
		ds.createfile("filename", 0)
		self.assertRaises(ValueError, ds.write, "filename", -1, 1)#negative offset
	
	def test_invalid_offset_on_read(self):
		ds = Device(1)
		ds.createfile("filename", 0)
		self.assertRaises(ValueError, ds.read, "filename", -1, 1)#negative offset

		experiment.ignore_IOB_on_read = False
		filesize = 10
		ds.createfile("filename2", filesize)
		self.assertRaises(ValueError, ds.read, "filename2", 9, 2)#offset beyond filesize
		
	def test_no_such_file_on_write(self):
		ds = Device(1)
		self.assertRaises(NoSuchFileError, ds.write, "filename", 0, 1)
		
	def test_no_such_file_on_read(self):
		ds = Device(1)
		self.assertRaises(NoSuchFileError, ds.read, "filename", 0, 1)

	def test_offset_beyond_filesize_on_write(self):
		ds = Device(1)
		ds.createfile("filename", 0)
		op_length = 1
		offset = 1
		actual_write = ds.write("filename", offset, op_length)
		self.assertEquals(actual_write, op_length)
		
if __name__ == '__main__':
	unittest.main()