#!/bin/bash

# 1. sort the events in time ascending order

TEMP_DIR=$1

sort -T $TEMP_DIR -n -k 6,6.17
