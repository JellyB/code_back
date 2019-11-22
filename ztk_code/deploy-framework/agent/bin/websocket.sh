#!/bin/sh
agent_home=/servers/agent
websocket_pid=${HOME}/run/websocket.pid
websocket_log=/data/logs/websocket
server_gc_log=${websocket_log}/gc.log
server_jvm_args="-server -Xmx1024m -Xms1024m -Xss256k -XX:MaxPermSize=128m  -Dserver_ip=__server_ip"

if [ ! -d ${websocket_log} ]
then
    mkdir -p ${websocket_log}
fi

#################
###set jdk7 env##
JAVA_HOME=/servers/agent/jdk7
echo ${JAVA_HOME}
PATH=$JAVA_HOME/bin:$PATH
CLASSPATH=.:$JAVA_HOME/lib:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar
export JAVA_HOME PATH CLASSPATH
#################

function start(){
	echo "starting websocket client "
	export CLASSPATH=${agent_home}/lib/*
	server_main_class=com.sohu.scm.agent.streaming.WebSocketServer
	java  ${server_jvm_args} -Dserver_name=scm-websocket ${server_main_class} >> ${websocket_log}/websocket.log  2>>${websocket_log}/websocket.err &
	pid=$!
	sleep 1
	ps $pid  > /dev/null
	if [ $? == 0 ]
	then
	    echo $pid > ${websocket_pid}
	    echo "Success pid:$pid"
	else
	    echo "Fail,see log ${websocket_log}/websocket.err"
	fi
	echo "pid: "${pid}
}

function stop_server(){
	echo "stoping websocket client"
	if [ -f ${websocket_pid} ]
	then
	    pid=`cat ${websocket_pid}`
	    if [ -z $pid ]
	    then
	        echo "can't kill the empty pid for ${websocket_pid}"
	    else
	        echo "kill -9 ${pid}"
	        kill -9 $pid
	        echo > ${websocket_pid}
	    fi
	else
	    echo "can't find the pid file ${websocket_pid}"
	fi
}

case "$1" in
    start)
       start
    ;;
    stop)
       stop_server
    ;;
    restart)
        stop_server
        sleep 2
        start
    ;;
    *)
        echo "Usage ${0} <start|stop|restart>"
        exit 1
esac
