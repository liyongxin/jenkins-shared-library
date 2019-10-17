#!/usr/bin/env groovy
/**
 * @author: liyongxin
 * @Date: 2019-10-14
 */

import cn.ctyun.devops.Build
import cn.ctyun.devops.Deploy
import cn.ctyun.devops.Sonar
import cn.ctyun.devops.WeChat

/**
 * docker image builder
 * @param dockerfile, path of dockerfile
 * @param context, path of build context
 * @param address, image address
 * @param tag, image tag
 * @param credentialsId, credential for image push
 * @return
 */
static def dockerBuild(String dockerfile = "Dockerfile", String context = ".", String address = "harbor.ctyuncdn.cn/devops", String tag = "latest", String credentialsId = "") {
    return new Build().build(dockerfile, context, address, tag, credentialsId)
}

/**
 * kubernetes deployer
 * @param resourcePath, path of deployment|ingress|service|configmap...
 * @param controllerFilePath, path of deployment
 * @param watch, weather watch controller resource creation
 * @param timeoutMinutes, totalTime for pod creation ready
 * @param sleepTime, time interval for watch pod creation
 * @param kind, resource controller type, deployment only for now
 * @return
 */
static def deploy(String resourcePath, String controllerFilePath, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    return new Deploy().deploy(resourcePath, controllerFilePath, watch, timeoutMinutes, sleepTime, kind)
}

/**
 * code scan with sonarqube
 * @param debug, if scan with param -X
 * @param waitScan, weather wait for code scan done
 * @param interupt, weather interupt pipeline if code scan Quality Gate result is not OK
 * @return
 */
static def scan(Boolean debug = true, Boolean waitScan = true, Boolean interupt = true) {
    return new Sonar().scan(debug, waitScan, interupt)
}

def notificationSuccess(project, credentialsId, title="", version="") {
    // msg = "查看Jenkins流水线历史记录"
    msg = "✅ ${title} ✅"
    // if (version != "") {
    // msg = "version: ${version} --- ${msg}"
    //   msg = "${msg} - version: ${version}"
    // }
    if (title == "") {
        title = "流水线成功了"
    } else if (title == "上线啦") {
        msg = "${msg} 🎉🎊🎈"
    }
    title = "${project}：${version}"

    msg = genNotificationMessage(msg, title)
    def buttons = getButtonLinks()
    msg = "${msg}${buttons}"
    // new Ding().markDown(title, msg, false, credentialsId)
    try {
        new WeChat().markDown(credentialsId, msg, true)
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
    def gitbranch = ""
    try {
        gitbranch = env.BRANCH_NAME
        if (gitbranch != null && gitbranch.startsWith("PR-")) {
            sh "git branch | grep '*' > gitbranch.file"
            gitbranch = readFile "gitbranch.file"
            gitbranch = gitbranch.replace("*", "").replace(" ", "")
        }
        if (gitbranch == null || gitbranch == "") {
            gitbranch = env.BRANCH_NAME
        }
    } catch (Exception ignored) {}
    if (env.CHANGE_TITLE != null && env.CHANGE_TITLE != "") {
        msg = "${msg}  \n  **Change**: ${env.CHANGE_TITLE}"
    }
    if (env.CHANGE_AUTHOR_DISPLAY_NAME != null && env.CHANGE_AUTHOR_DISPLAY_NAME != "") {
        msg = "${msg}  \n  **Author**: ${env.CHANGE_AUTHOR_DISPLAY_NAME}"
    }
    if (env.CHANGE_AUTHOR != null && env.CHANGE_AUTHOR != "") {
        msg = "${msg}  \n  **Author**: ${env.CHANGE_AUTHOR}"
    }
    if (env.CHANGE_AUTHOR_EMAIL != null && env.CHANGE_AUTHOR_EMAIL != "") {
        msg = "${msg}  \n  **Author**: ${env.CHANGE_AUTHOR_EMAIL}"
    }
    if (gitlog != null && gitlog != "") {
        msg = "${msg}  \n  **Git log**: ${gitlog}"
    }
    if (gitbranch != null && gitbranch != "") {
        msg = "${msg}  \n  **Git branch**: ${gitbranch}"
    }
    if (env.CHANGE_TARGET != null && env.CHANGE_TARGET != "") {
        msg = "${msg}  \n  **Merge target**: ${env.CHANGE_TARGET}"
    }

    return msg
}

def genButtons(isEnvironment=false) {
    buttons = [
            [
                    "title": "查看流水线",
                    "actionURL": "${env.BUILD_URL}"
            ]
    ]
    if (env.CHANGE_URL != null && env.CHANGE_URL != "") {
        buttons.add([
                "title": "查看PR",
                "actionURL": "${env.CHANGE_URL}"
        ])
    }
    if (isEnvironment) {
        buttons.add([
                "title": "环境配置",
                "actionURL": "http://confluence.alaudatech.com/pages/viewpage.action?pageId=23388567"
        ])
    }
    return buttons
}

def getButtonLinks(isEnvironment=false) {
    def msg = ""
    def listT = genButtons(isEnvironment)
    listT.each() {
        if (msg == "") {
            msg = "  \n > "
        }
        msg = "${msg} --- ["+it["title"]+"]("+it["actionURL"]+") "
    }
    return msg
}
