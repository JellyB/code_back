#!/bin/bash

server_name="${server_name}"
server_home="/servers/${server_name}"
server_log_home="/data/logs/${server_name}"
server_args="${main_args}"
server_jvm_args="${jvm_args}"
server_main_class="${main_class}"
server_resources="${server_home}/resources/"
server_mode="${server_mode}"

echo "##################################################################"
echo "server_ip=${server_ip}"
echo "server_name=${server_name}"
echo "server_home=${server_home}"
echo "server_log_home=${server_log_home}"
echo "server_args=${server_args}"
echo "server_jvm_args=${server_jvm_args}"
echo "server_main_class=${server_main_class}"
echo "server_resources=${server_resources}"
echo "server_mode=${server_mode}"
echo "####################################################################"

echo "start update the server[${server_name}] instance"
server_home="/servers/${server_name}"
server_log_home="/data/logs/${server_name}"

cp template_server.sh ${server_home}/server.sh

cd ${server_home}/
## '#' is the separator
sed -i "s#__server_jvm_args#${jvm_args}#g" server.sh
sed -i "s#__server_args#${main_args}#g" server.sh
sed -i "s#__server_main_class#${main_class}#g" server.sh
sed -i "s#__server_name#${server_name}#g" server.sh
sed -i "s#__server_ip#${server_ip}#g" server.sh
sed -i "s#__server_mode#${server_mode}#g" server.sh

echo "update server[${server_name}] finished"
exit 0