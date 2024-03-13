
pipeline {
    agent any


    /**
     * 选择性参数
     */
    parameters {
        choice choices: ['1','4'], description: '此处服务器标识代表具体操作服务器', name: 'SERVER_ID'
    }

    environment {
        project_model = "712_qj5"
        svn_url = "svn://192.168.11.50/qj5"
        //工作目录
        work_path = "/data/game/server/game_${project_model}${SERVER_ID}"
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
                sh 'mvn clean -T 1C package -Dmaven.test.skip=true'
                // sh 'mvn clean install'
                //打包直接到当前目录下的libs
                sh 'mvn  dependency:copy-dependencies -DoutputDirectory=\'${env.WORKSPACE}\'/libs -DincludeScope=runtime'

            }
        }

    }
}
