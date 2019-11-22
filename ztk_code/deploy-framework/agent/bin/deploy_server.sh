#!/bin/sh
#server_name server_args server_jvm_args server_main_class
server_ip="__server_ip"
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

if [ ! -d ${server_home} ]; then
    echo "server not exist"
    exit 0
fi



cd ${server_home}

##command is success function
function command_is_success(){
    if [ ! $? -eq 0 ]; then
        echo $1
        exit 1
    fi
}

function get_server_pids() {
    pids=( `ps -eo pid,user,cmd | grep "${server_resources}" | grep "${server_main_class}" | awk '{print $1}'` )
}

##start server
function start(){
    echo "starting server ${server_name}"
    sh ${server_home}/server.sh start
}

##stop server
function stop(){
    echo "stoping server ${server_name}"
    sh ${server_home}/server.sh stop
}

##dump server
function dump(){
    echo "dump server ${server_name}"

}

case "$1" in
    deploy)
        rm -f ${server_home}/${source_path}
        echo "wget -cq http://192.168.100.20:8080/${source_path}"
        wget -cqO ${server_home}/${source_path} http://192.168.100.20:8080/${source_path}
        command_is_success "wget file ${source_path} fail."
        stop
        #clear dir
        rm -rf ${server_home}/lib ${server_home}/resources
        unzip -oq ${source_path} -d ${server_home}
        start
    ;;
    delete_server)
        echo "delete server ${server_name}"
        stop
        sleep 2
        #del server
        echo "delete dir: ${server_home}"
        rm -rf ${server_home}
        #del log dir
        echo "delete dir: ${server_log_home}"
        rm -rf ${server_log_home}
        echo "delete the server "${server_name}" succesfull"
    ;;
    start)
        start
    ;;
    stop)
        stop
    ;;
    dump)
        dump
    ;;
    restart)
        stop
        start
    ;;
    *)
        echo "Usage ${0} <start|stop|restart>"
        exit 1
    ;;
esac

exit 0