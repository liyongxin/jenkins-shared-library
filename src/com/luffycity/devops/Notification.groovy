package com.luffycity.devops



/**
 * notification
 * @param project
 * @param title
 * @param version
 * @param credentialsId
 * @return
 */
def notification(project, title="", version="", credentialsId=""){
    this.project = project
    this.title = title
    this.version = version
    this.credentialsId = credentialsId
    return this
}


def success() {
    def msg ="<font color=\"info\">😄👍 ${this.title} 👍😄</font>"
    if (this.title == "") {
        this.title = "<font color=\"info\">流水线成功了</font>"
    } else if (env.TAG_NAME) {
        msg = "🎉🎈 ${this.project}发布到测试环境成功了🎈🎉"
    }
    this.title = "${this.project}:"

    msg = genNotificationMessage(msg, this.title)
    def buttons = getButtonLinks(this.project)
    msg = "${msg}${buttons}"
    try {
        if (this.credentialsId == "dingTalk"){
            new DingTalk().markDown(this.title, msg, false, this.credentialsId)
        }else {
            // notification by other ways
        }
    } catch (Exception ignored) {}
}

/**
 *
 * @param project
 * @param title
 * @param version
 * @param credentialsId
 * @return
 */
def failure() {
    def msg = "<font color=\"warning\">😖❌ ${this.title} ❌😖</font>"
    if (this.title == "") {
        this.title = "<font color=\"warning\">流水线失败了,请及时查看！</font>"
    }else if (env.TAG_NAME) {
        msg = "😖❌ ${this.project}发布到测试环境失败了❌😖,请及时查看！"
    }
    this.title = "${this.project}:"
    msg = genNotificationMessage(msg, this.title)
    def buttons = getButtonLinks(this.project)
    msg = "${msg}${buttons}"
    try {
        if (this.credentialsId == "dingTalk"){
            new DingTalk().markDown(this.title, msg, true, this.credentialsId)
        }else {
            //notification by other ways
        }
    } catch (Exception ignored) {}

}

def genNotificationMessage(msg, title="") {
    if (title != "") {
        msg = "### ${title}  \n  ${msg}"
    }

    def gitlog = ""
    try {
        sh "git log --oneline -n 1 > gitlog.file"
        gitlog = readFile "gitlog.file"
    } catch (Exception ignored) {}
    def gitbranch = env.BRANCH_NAME

    if (gitlog != null && gitlog != "") {
        msg = "${msg}  \n  **Git log**: ${gitlog}"
    }
    if (gitbranch != null && gitbranch != "") {
        msg = "${msg}  \n  **Git branch**: ${gitbranch}"
    }
    msg = "${msg}  \n  **Build Tasks**: ${env.BUILD_RESULT}"
    return msg
}


def getButtonLinks(project="") {
    def msg = ""
    def listT = genButtons(project)
    listT.each() {
        if (msg == "") {
            msg = "  \n > "
        }
        msg = "${msg} --- ["+it["title"]+"]("+it["actionURL"]+") "
    }
    return msg
}

def genButtons(project="") {
    buttons = [
            [
                    "title": "查看流水线",
                    "actionURL": "${env.RUN_DISPLAY_URL}"
            ],
            [
                    "title": "代码扫描结果",
                    "actionURL": "http://sonar.devops.cn/dashboard?id=${project}"
            ]
    ]
    if (env.TAG_NAME != "" && env.TAG_NAME != null) {
        buttons.add([
                "title": "验收测试结果",
                "actionURL": "${env.ACCEPT_TEST_URL}artifact/artifacts/report.html"
        ])
    }
    return buttons
}
