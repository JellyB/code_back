#!/bin/sh
ztk_deploy_home="/servers/ztk-deploy"
git pull
mvn  clean package -U -Dmaven.test.skip=true -Dmaven.compile.fork=true -pl server-console -am -P online -Dprofile=online
rm -rf $ztk_deploy_home/lib $ztk_deploy_home/resources
unzip server-console/target/server-console-1.0-SNAPSHOT-dist.zip -d $ztk_deploy_home/
chmod 755 $ztk_deploy_home/resources/webapp/WEB-INF/bin/*.sh
sh /servers/ztk-deploy/server.sh restart
echo "deploy server console finished."

exit 0
