export JAVA_HOME=/usr/local/openjdk-21/;
export JRE_HOME=${JAVA_HOME}/jre;
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib;
export PATH=${JAVA_HOME}/bin:${PATH};
java -version
rm -rfv /root/.m2/repository/org/wxd/*
/usr/local/apache-maven-3.8.5/bin/mvn clean package -f pom.xml -Dmaven.test.skip=true
if [ $? != 0 ]; then
  echo "打包失败"
  exit 1
fi