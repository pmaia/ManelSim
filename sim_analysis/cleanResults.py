# -*- coding: utf-8 -*-
import sys
import io

# This script takes the output of concatResults.sh and clean it. For example

#
#	29468777916	29468777916	10966164	10966164
#	2631401392	2631401392	741520	741520
#	29468777916	29468777916	10966164	10966164
#	2631401392	2631401392	741520	741520
#
#   Will generate
#
#	29468777916	29468777916	10966164	10966164 2631401392	2631401392	741520	741520
#	29468777916	29468777916	10966164	10966164 2631401392	2631401392	741520	741520

lines = sys.stdin.readlines()

for index in range(0, len(lines) - 1, 2):
    sys.stdout.write(lines[index].strip() + "\t" + lines[index+1].strip() + "\n")

sys.stdout.flush()
sys.stdout.close()

