# -*- coding: utf-8 -*-

import sys
import io

"""  """
def parse_line(tokens):
   op = tokens[0]
   if op == 'open':
       return tokens[2]
   elif op == 'write':
       return tokens[1]
   elif op == 'close':
       return tokens[1]
   elif op == 'read':
       return tokens[1]
   return None
  
"""  """
def fromString2Time(stamp):
   splited = stamp.split(".")
   return (int(splited[0]), int(splited[1]))

def stamp_as_millis(seconds, microsecs):#(secs, micro)
   return (seconds * 1000) + (microsecs / 1000)

def new_line(tokens, new_stamp):
   op = tokens[0]
   if op == 'open':
       new_tokens = list(tokens)
       new_tokens.pop(2)
       new_tokens.insert(2, str(new_stamp))
       return new_tokens
   elif op == 'write':
       new_tokens = list(tokens)
       new_tokens.pop(1)
       new_tokens.insert(1, str(new_stamp))
       return new_tokens
   elif op == 'close':
       new_tokens = list(tokens)
       new_tokens.pop(1)
       new_tokens.insert(1, str(new_stamp))
       return new_tokens
   elif op == 'read':
       new_tokens = list(tokens)
       new_tokens.pop(1)
       new_tokens.insert(1, str(new_stamp))
       return new_tokens
   return None

# Main script     
old_format_first_stamp = None

for line in sys.stdin:
   if not line.strip() == "":        
       tokens = line.split()
       parsed = parse_line(tokens)
       if not parsed is None:
           (seconds, micro) = fromString2Time(parsed)
           if old_format_first_stamp == None:
               old_format_first_stamp = (seconds, micro)
           stamp = stamp_as_millis(seconds - old_format_first_stamp[0], micro - old_format_first_stamp[1])
           print "\t".join(new_line(tokens, stamp))