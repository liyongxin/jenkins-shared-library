package com.luffycity.devops

import groovy.json.JsonOutput


def markDown(title, text, isAt = false, botUrlCredentialsId, Boolean verbose=false) {
    String atSymbol = ""
    String atAccount = ""
    String extraAccount = ""
    if (isAt) {
        atAccount = "${env.NOTIFY_ACCOUNT}" or "luffy"
        atSymbol = "@"
    }
    data = [
        "msgtype": "markdown",
        "markdown": [
            "title": title,
            "text": text + "${atSymbol}${atAccount}"
        ],
        "at": [
            "atMobiles": [
                    "${atAccount}"
            ],
            "isAtAll": false
        ]
    ]
    this.sendRequest("POST", data, botUrlCredentialsId, verbose)
}

def sendRequest(method, data, botUrlCredentialsId, Boolean verbose=false, codes="100:399") {
    def reqBody = new JsonOutput().toJson(data)
    echo "123"
    echo reqBody
    withCredentials([usernamePassword(credentialsId: botUrlCredentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        httpRequest(
            httpMode:method, url: "https://oapi.dingtalk.com/robot/send?access_token=${PASSWORD}",
            requestBody:reqBody,
            validResponseCodes: codes,
            contentType: "APPLICATION_JSON",
            quiet: !verbose
        )
    }
}