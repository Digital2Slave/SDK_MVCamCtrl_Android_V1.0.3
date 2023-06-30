#!/bin/sh
# ***************************************************************************************
#
#
# ***************************************************************************************


echo "Setting usbfs memory size to 500"
sh -c 'cat /sys/module/usbcore/parameters/usbfs_memory_mb'
sh -c 'echo 500 > /sys/module/usbcore/parameters/usbfs_memory_mb'
sh -c 'cat /sys/module/usbcore/parameters/usbfs_memory_mb'

