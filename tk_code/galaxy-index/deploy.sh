#!/bin/bash
git pull
mvn package -Dmaven.test.skip=true
cp index-server/target/index-server.jar /var/app/index-server/
rm -rf index-server/target/
ln -s /var/app/index-server/index-server.jar /etc/init.d/index-server
/etc/init.d/index-server restart --spring.profiles.active=prod