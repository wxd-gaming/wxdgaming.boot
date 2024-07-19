/**飞书通知 */
def feishu(String feishu_url, String context, String BUILD_USER) {
    try {
        def object = [msg_type: "text", content: [text: "${context} \n${BUILD_USER}"]];
        def jsonString = groovy.json.JsonOutput.toJson(object)
        sh "curl --retry 2 -X POST -H 'Content-Type: application/json' -d '$jsonString' '${feishu_url}'"
    } catch (ignore) {
        echo "上报飞书失败 ${ignore}"
    }
}


return this