#!/bin/sh
dist="/data/projects/dist-source"
sohu_scm_home="/data/projects/projects/deploy-framework/"
cd $sohu_scm_home
git pull
mvn  clean package -Dmaven.test.skip=true -Dmaven.compile.fork=true -pl agent -am
cd agent 
cp -f bin/init_env.sh ${dist}/script/
rm -f ${dist}/agent_current.zip
cp -f target/agent-1.0-SNAPSHOT-dist.zip ${dist}/agent_current.zip
echo "deploy agent finish"
exit 0

