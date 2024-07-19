pipeline {
    agent any

    /**
     * 选择性参数
     */
    parameters {
        choice choices: ['1', '4'], description: '此处服务器标识代表具体操作服务器', name: 'SERVER_ID'
    }

    environment {
        //全局参数
        project_model = "712_qj5"
        svn_url = "svn://192.168.11.50/qj5"
        // 预定义的环境变量
        JAVA_HOME = '/usr/local/jdk-21'
        PATH = "$JAVA_HOME/bin:$PATH"
        CLASSPATH = ".:${env.JAVA_HOME}/lib/dt.jar:${env.JAVA_HOME}/lib/tools.jar"

    }

    stages {
        stage('显示选择内容') {
            steps {
                echo '更新内容------------------------------------------'
                echo 'branch：'
                echo "${branch}"
            }
        }

        stage('git pull') {
            steps {
                echo '拉取代码'
                checkout([$class: 'GitSCM', branches: [[name: "${branch}"]], extensions: [], userRemoteConfigs: [[url: 'https://gitee.com/wxd-gaming/org.wxd.boot.git']]])
                // sh 'cd /data/compile/src/engine712 && git checkout develop  && git pull'

            }
        }

        stage('compile and package') {
            steps {
                //sh 'cd /data/compile/src/engine712 && mvn clean package'
                echo '当前目录'
                sh 'pwd'
                echo '转换sh文件格式'
                sh 'find ./ -name "*.sh" | xargs dos2unix'
                echo '开始编译'
                sh '''
mvn clean package -Dmaven.test.skip=true
'''
                def fs_util = load("builder/feishu.groovy")
                fs_util.feishu("feishu.open.com", "编译成功", "wuxin")
            }
        }

    }
}
