#!/bin/bash

# 1. removes most of the events that are not of interest of the simulation

grep -v S_IFSOCK | grep -P "sys_(open|close|write|read|unlink)|do_filp_open" 
