# -*- coding: utf-8 -*-
#
#  Copyright (C) 2009 Universidade Federal de Campina Grande
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
# 
#          http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import random
import sys

class FileSizeDistribution:

	def __init__(self, mean, std_dev, maxsize):
		self.maxsize = maxsize
		self.mean = mean
		self.std_dev = std_dev

	def sample(self):
		return long(min(random.lognormvariate(self.mean, self.std_dev), self.maxsize))

class Log:

	def __init__(self):
		self.op_traces = {}
		self.read_traces = [0, 0, 0, 0]# (totalbytes_co-located, total bytes, totalops_co-located, total_ops)
		self.write_traces = [0, 0, 0, 0]
		self.file2traces = {}
		self.latency_logs = []
		self.migrated_files = []
		self.login_stamps = []

	def log_report(self, filename, op_type, length, waslocal, time, now):
		if not filename in self.file2traces:
			trace = {}
			trace["read"] = [0, 0, 0, 0]
			trace["write"] = [0, 0, 0, 0]
			self.file2traces[filename] = trace
		trace = self.file2traces[filename]
		
		if op_type == "read":
			self.__update_trace(self.read_traces, waslocal, length)
			self.__update_trace(trace["read"], waslocal, length)	
		elif op_type == "write":
			self.__update_trace(self.write_traces, waslocal, length)
			self.__update_trace(trace["write"], waslocal, length)
		else:
			raise ValueError()

		self.latency_logs.append((filename, op_type, length, waslocal, time, length/time, now))
		if len(self.latency_logs) == 10.000:
			self.print_latency(self.latency_logs)
			self.latency_logs = []

	def report_new_login(self, device, timestamp):
		self.login_stamps.append((device, timestamp))
	
	def print_login(self, login_logs):
		for (device, timestamp) in login_logs:
			sys.stderr.write(str(device.ds_id) + "\t" + str(timestamp) + "\n")

	def report_migration(self, filename):
		self.migrated_files.append(filename)

	def print_migration(self, migration_logs):
		for migration_line in migration_logs:
			sys.stderr.write(migration_line + "\n")

	def print_latency(self, latency_logs):
		pass
		for (filename, op_type, length, waslocal, time, through, stamp) in latency_logs:
			sys.stderr.write("\t".join([filename, str(op_type), str(length), str(waslocal), str(time), str(through)]) + "\t" + str(stamp) + "\n")

	def __update_trace(self, trace, waslocal, length):
		# (totalbytes_co-located, total bytes, totalops_co-located, total_ops)
		if (waslocal):
			trace[0] = trace[0] + length
			trace[1] = trace[1] + length
			trace[2] = trace[2] + 1
			trace[3] = trace[3] + 1 
		else: 
			trace[1] = trace[1] + length
			trace[3] = trace[3] + 1
		
	def __str__(self):
		self.print_latency(self.latency_logs)
#		self.print_migration(self.migrated_files)
#		self.print_login(self.login_stamps)

		r_summary = "\t".join([str(self.read_traces[0]), str(self.read_traces[1]),\
						str(self.read_traces[2]), str(self.read_traces[3])])
		w_summary = "\t".join([str(self.write_traces[0]), str(self.write_traces[1]), \
						str(self.write_traces[2]), str(self.write_traces[3])])
		trace_lines = []
		for (filename, trace) in self.file2traces.iteritems():
			read_t = trace["read"]
			write_t = trace["write"]
			r_str = "\t".join([str(read_t[0]), str(read_t[1]), str(read_t[2]), str(read_t[3])])
			w_str = "\t".join([str(write_t[0]), str(write_t[1]), str(write_t[2]), str(write_t[3])])
			trace_lines.append("\t".join([filename, r_str, w_str]))
		trace_str = "\n".join(trace_lines)

		return "\n".join(["#", r_summary, w_summary, trace_str])

class Node(object):
	__slots__ = ['prev', 'next', 'me']
	def __init__(self, prev, me):
		self.prev = prev
		self.me = me
		self.next = None

class LRU:
	"""
	Implementation of a length-limited O(1) LRU queue.
	Built for and used by PyPE:
	http://pype.sourceforge.net
	Copyright 2003 Josiah Carlson.
	"""
	def __init__(self, count, pairs=[]):
		self.count = max(count, 1)
		self.d = {}
		self.first = None
		self.last = None
		for key, value in pairs:
			self[key] = value
	def __contains__(self, obj):
		return obj in self.d
	def __getitem__(self, obj):
		a = self.d[obj].me
		self[a[0]] = a[1]
		return a[1]
	def __setitem__(self, obj, val):
		if obj in self.d:
			del self[obj]
		nobj = Node(self.last, (obj, val))
		if self.first is None:
			self.first = nobj
		if self.last:
			self.last.next = nobj
		self.last = nobj
		self.d[obj] = nobj
		if len(self.d) > self.count:
			if self.first == self.last:
				self.first = None
				self.last = None
				return
			a = self.first
			a.next.prev = None
			self.first = a.next
			a.next = None
			del self.d[a.me[0]]
			del a
	def __delitem__(self, obj):
		nobj = self.d[obj]
		if nobj.prev:
			nobj.prev.next = nobj.next
		else:
			self.first = nobj.next
		if nobj.next:
			nobj.next.prev = nobj.prev
		else:
			self.last = nobj.prev
		del self.d[obj]
	def __iter__(self):
		cur = self.first
		while cur != None:
			cur2 = cur.next
			yield cur.me[1]
			cur = cur2
	def iteritems(self):
		cur = self.first
		while cur != None:
			cur2 = cur.next
			yield cur.me
			cur = cur2
	def iterkeys(self):
		return iter(self.d)
	def itervalues(self):
		for i,j in self.iteritems():
			yield j
	def keys(self):
		return self.d.keys()
