#!/bin/sh

server_home="/servers/${server_name}"
server_log_home="/data/logs/${server_name}"
cd /servers/
if [ -d "${server_home}" ]; then
    ${serverName}/server.sh stop
    echo "delete server ${server_name}"
    #del server
    rm -rf ${server_home}
    #del log dir
    rm -rf ${server_log_home}
    echo "delete the "${server_name}" succesfull"
    exit 0
fi
exit 0