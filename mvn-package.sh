#!/bin/bash
 
# 设置JAVA_HOME环境变量
export JAVA_HOME=/usr/local/jdk-21
# 将JAVA_HOME加入到PATH变量中
export PATH=$JAVA_HOME/bin:$PATH
# 打印出Java版本信息以验证设置是否成功
java -version
#cd /mnt/e/work/engine712-server
mvn clean kotlin:compile package -Dmaven.test.skip=true -pl wxdgaming.boot.kotlin -am

