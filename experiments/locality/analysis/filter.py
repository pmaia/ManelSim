#!/bin/python
import sys

def is_io_line(line):
    return ("simulation.beefs.model.FileSystemClient.read" in line) or\
           ("simulation.beefs.model.FileSystemClient.write" in line)

def format_io_line(line):
#2012-11-29 15:24:27,707 INFO  FileSystemClient simulation.beefs.model.FileSystemClient.read(FileSystemClient.java:62) - op=read client_host=machine-7 ds_host=machine-11 filepath=/home/isabellylr/Downloads/broker/lib/ourgrid-4.2.6.jar/ bytesTransfered=380 begin=1318871274665823 duration=157
    tokens = line.split()
    op_type, client, ds, length = tokens[6].split("=")[1], tokens[7].split("=")[1],\
                                      tokens[8].split("=")[1], tokens[10].split("=")[1]
    return [op_type, client, ds, length]

if __name__ == "__main__":
    """
       It filter out and formats ManelSim log lines
       usage: python filter.py < log > outfile
       outfile format: op_type\tclient\tds\length
    """
    for logline in sys.stdin:
        if is_io_line(logline):
            print "\t".join(format_io_line(logline))
