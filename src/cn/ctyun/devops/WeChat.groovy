package cn.ctyun.devops

import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput

def markDown(credentialsId, text,  Boolean verbose=false) {
    data = [
        'msgtype': 'markdown',
        'markdown': [
            'content': "${text}"
        ],
    ]
    this.sendMessage(data,credentialsId, verbose)
}

def sendMessage(data, credentialsId, Boolean verbose=false, codes="100:399") {
//    def accessToken = this.getToken(credentialsId, verbose)
//    if (accessToken == '') {
//        echo "Access token not fetched..."
//        return
//    }
    def reqBody = new JsonOutput().toJson(data)
    def response = httpRequest(
        httpMode:'POST', url: "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=${accessToken}", 
        requestBody:reqBody, 
        validResponseCodes: codes,
        contentType: "APPLICATION_JSON",
        quiet: !verbose
    )
    def jsonSlurper = new JsonSlurperClassic()
    def json = jsonSlurper.parseText(response.content)
    echo "json response: ${json}"
    return json
}

def getToken(credentialsId, Boolean verbose=false, codes="100:399") {
    def response = null
    withCredentials([usernamePassword(credentialsId: credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        response = httpRequest(
            httpMode:'GET', url: "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=${USERNAME}&corpsecret=${PASSWORD}", 
            validResponseCodes: codes,
            quiet: !verbose
        )
    }
    if (response != null && response.content != null) {
        def jsonSlurper = new JsonSlurperClassic()
        def json = jsonSlurper.parseText(response.content)
        return json.access_token
    }
    return ''
}