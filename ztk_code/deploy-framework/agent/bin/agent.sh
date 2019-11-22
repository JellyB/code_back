#!/bin/sh

agent_log=/data/logs/agent
server_gc_log=${agent_log}/gc.log
server_args="server /servers/agent/conf/agent.yml"
server_instance_home=/servers/
server_jvm_args="-server -Xmx1024m -Xms1024m -Xss256k    -Dserver_ip=__server_ip"
server_main_class=com.huatu.ztk.scm.agent.AgentDaemon
server_class_path="";
server_home=/servers/agent
version=2.0


if [ ! -d ${agent_log} ]
then
    mkdir -p ${agent_log}
fi

 #agent_pids=('ps -eo pid,user,cmd|grep java |grep scm-agent | awk -F " " '{print $1;}'')
#
get_agent_pid(){
    agent_pids=( `ps -eo pid,user,cmd | grep "java" | grep "${server_main_class}" | awk '{print $1}'` )
}

get_agent_pid

case "$1" in
    start)
        echo "starting agent client"

        #iterator server jar libs
        for i in `ls ${server_home}/lib | sort -rf`
        do
            server_class_path=$server_class_path:${server_home}/lib/$i
        done
        export CLASSPATH=${server_class_path}
        echo ${exist}
        if [ ${#agent_pids[*]} -gt 0 ]; then
            echo "agent is started,the pid is :${agent_pids[@]}"
            exit 1
        fi
        start_time=`date "+%Y-%m-%d %H:%M:%S"`
        java  ${server_jvm_args} -Dagent_version=${version}  -Dstart_time="${start_time}" -Duser.timezone=Asia/Harbin -Dserver_name=agent -Dserver_log_home=${agent_log} ${server_main_class} ${server_args} >> ${agent_log}/agent.log  2>>${agent_log}/agent.err &
        sleep 3
        get_agent_pid
        if [ ${#agent_pids[*]} -gt 1 ]; then
            echo "agent is start success,but find multiple instances pids: ${agent_pids[@]}"
            exit 1
        elif [ ${#agent_pids[*]} -eq 1 ]; then
            echo "agent is start success,the pid is ${agent_pids[@]}"
        else
            echo "Fail,see log ${agent_log}/agent.err"
            exit 1
        fi
    ;;
    stop)
        echo "stoping agent client pids:${agent_pids[@]}"
        if [ ${#agent_pids[*]} -gt 0 ]; then
            echo "kill -9 ${agent_pids[@]}"
            kill -9 ${agent_pids[@]}
            get_agent_pid
            if [ ${#agent_pids[*]} -gt 0 ]; then
                echo "stop agent fail. pid is : ${agent_pids[@]}"
                exit 1
            else
                echo "stop agent success."
            fi
        else
            echo "agent not started"
        fi
    ;;
    restart)
        $0 stop
        sleep 2
        $0 start
    ;;
    update)
        echo "update agent and restart"
        if [ ! $# -eq 2 ]; then
            echo "update agent fail"
            echo "update usage ${0} update current_ip"
            exit 1
        fi
        cd /servers/agent/
         wget http://192.168.100.20:8080/agent_current.zip

        if [ ! $? == 0 ]; then
            echo "wget file agent_current.zip fail."
            exit $?
        fi

        rm -rf /servers/agent/lib /servers/agent/bin /servers/agent/conf
        unzip -o agent_current.zip
        rm -rf agent_current.zip
        cd bin
        sed -i "/__server_ip/{s/__server_ip/${2}/;:skip n;b skip;}" deploy_server.sh
        sed -i "/__server_ip/{s/__server_ip/${2}/;:skip n;b skip;}" agent.sh
        chmod +x  *.sh
        ./agent.sh restart
    ;;
    *)
        echo "Usage ${0} <start|stop|restart>"
        exit 1
esac