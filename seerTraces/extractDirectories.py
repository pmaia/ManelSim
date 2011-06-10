import sys
import io

trace_path = sys.argv[1]
trace = open(trace_path, 'r')

direc_trace = open(trace_path + '.directories' , 'w')
directories_dict = dict()

for line in trace:
   splited = line.split()
   if splited[1] == 'UID':
      direct_splited = line.split('"')
      if len(direct_splited) > 1 :
         print direct_splited
         direct_name = direct_splited[1].split('/')[0]
         if direct_name.find('.') == -1 :
            directories_dict[direct_name] = direct_name

for (k, v) in directories_dict.iteritems():
   direc_trace.writelines(k + '\n')

direc_trace.flush()
direc_trace.close()

trace.close()
