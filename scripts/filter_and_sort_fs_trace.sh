#!/bin/bash

# 1. filter only events of interest from the raw trace
# 2. sort the events in time ascending order

grep -P "sys_[close|write|read|unlink]" | sort -n -k 6,6.17
