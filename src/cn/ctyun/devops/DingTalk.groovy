package cn.ctyun.devops

import groovy.json.JsonSlurperClassic
import groovy.json.JsonOutput


def sendRequest(method, data, botUrlCredentialsId, Boolean verbose=false, codes="100:399") {
    def reqBody = new JsonOutput().toJson(data)
    withCredentials([usernamePassword(credentialsId: botUrlCredentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        def response = httpRequest(
            httpMode:method, url: "https://oapi.dingtalk.com/robot/send?access_token=${PASSWORD}",
            requestBody:reqBody, 
            validResponseCodes: codes,
            contentType: "APPLICATION_JSON",
            quiet: !verbose
        )
    }
}


def sendLink(title, text, url, botUrlCredentialsId, picUrl="", verbose=false) {
    data = [
        "link": [
            "title": title,
            "text": text,
            "picUrl": picUrl,
            "messageUrl": url,
        ],
        "msgtype": "link"
    ]
    // data = [
    //     "markdown": [
    //         "title": title,
    //         "text": "#### ${title}\n>${text}\n>![icon](${picUrl})\n> ###### [查看](${url})",
    //     ],
    //     "msgtype": "markdown"
    // ]
    // http://icons.iconarchive.com/icons/paomedia/small-n-flat/1024/sign-error-icon.png
    this.sendRequest("POST", data, botUrlCredentialsId, verbose)
}

def actionCard(title, text, buttonText, buttonUrl, botUrlCredentialsId, buttons=null, Boolean verbose=false) {
    if (buttons == null) {
        buttons = [
                [
                    "title": buttonText,
                    "actionURL": buttonUrl
                ]
            ]
    }
    data = [
        "actionCard": [
            "title": title,
            "text": text,
            "hideAvatar": "0",
            "btnOrientation": "0",
            "btns": buttons
        ],
        "msgtype": "actionCard"
    ]
    this.sendRequest("POST", data, botUrlCredentialsId, verbose)
}

def markDown(title, text, isAt = false, botUrlCredentialsId, Boolean verbose=false) {
    String atSymbol = ""
    String atAccount = ""
    if (isAt) {
        atAccount = "${env.NOTIFY_ACCOUNT}"
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