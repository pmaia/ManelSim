#!/bin/bash

# 1. removes most of the events that are not of interest of the simulation

grep -P -v "S_IFIFO|S_IFSOCK|S_IFBLK|S_IFCHR" | grep -P "sys_(open|close|write|read|unlink)|do_filp_open" 
