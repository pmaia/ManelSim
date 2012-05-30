#!/bin/bash


while true; do
	date +%s
	mpstat
	cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq
	cat /proc/acpi/battery/BAT0/state
	sleep 1
done
