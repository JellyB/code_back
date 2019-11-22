#!/bin/sh

#########################################
#### init the server instance evn #######
#########################################

## $1: user $2: ip

function usage(){
  echo "Usage:$0 <usr_name>"
}

function add_user()
{
    name=$1
    if [ "$name" == "" ]
    then
      usage
      exit
    fi
    home=/data/$name

    echo "Create new user $name,home:$home"
    adduser -m -d "$home" $name
    ln -s /data/$name /home/$name

    #init the .bash_profile

    echo "init the .bash_profile"
    echo "export LANG=en_US.UTF-8" >> /home/${name}/.bash_profile
    chown "${name}.${name}" /home/$name
}

function init_dir(){
    mkdir -p /servers/agent
    mkdir -p /data/logs/agent
    echo "init dir finished"
}

function init_agent(){
    cd /servers/agent
    rm -f agent_current.zip
    wget http://192.168.100.20:8080/agent_current.zip

    unzip agent_current.zip
    rm -f agent_current.zip
}

#########################  get start ###########################

base=`dirname $0`
u_root=`whoami`
user=$1

if [ ${u_root} != "root" ] ;then
    echo "u must be [root] user"
    exit 1
fi

if id -u ${user} >/dev/null 2>/dev/null; then
        echo "user ${user} exists"
        exit 1
else
        echo "user ${user} does not exist, u can install user[${user}] now"
fi

#add user
#add_user ${user}

#init the smc user directory
#echo "init the smc ${user} directory"
init_dir ${user}

#init agent
init_agent ${user} $2

#chown -R "${user}.${user}" /opt/${user}/
#cd /home/${user}/agent/bin
echo "init the server instance Finished."
