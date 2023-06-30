#!/bin/sh
# ***************************************************************************************
#     set_socket_buffer_size.sh
# ***************************************************************************************


sh -c 'cat /proc/sys/net/core/wmem_max'
echo "Setting socket memory size to 10485760"
sh -c 'echo 10485760 > /proc/sys/net/core/wmem_max'
sh -c 'cat /proc/sys/net/core/wmem_max'