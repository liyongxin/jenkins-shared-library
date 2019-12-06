#!/usr/bin/env groovy
/**
 * @author: liyongxin
 * @Date: 2019-10-14
 */

import cn.ctyun.devops.Build
import cn.ctyun.devops.Deploy
import cn.ctyun.devops.Sonar
import cn.ctyun.devops.WeChat
import cn.ctyun.devops.DingTalk
import cn.ctyun.devops.Utils

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
static def deploy(String resourcePath, String controllerFilePath, String imageTag, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    return new Deploy().deploy(resourcePath, controllerFilePath, imageTag, watch, timeoutMinutes, sleepTime, kind)
}

/**
 * code scan with sonarqube
 * @param projectVersion, projectVersion for sonarqube
 * @param debug, if scan with param -X
 * @param waitScan, weather wait for code scan done
 * @param interupt, weather interupt pipeline if code scan Quality Gate result is not OK
 * @return
 */
static def scan(String projectVersion="", Boolean debug = true, Boolean waitScan = true, Boolean interupt = true) {
    return new Sonar().scan(projectVersion, debug, waitScan, interupt)
}

/**
 *
 * @param project
 * @param title
 * @param version
 * @param credentialsId
 * @return
 */
def notificationSuccess(project, title="", version="", credentialsId="wechatBot") {
    // msg = "查看Jenkins流水线历史记录"
    msg = "<font color=\\\"info\\\">✅ ${title} ✅</font>"
    // if (version != "") {
    // msg = "version: ${version} --- ${msg}"
    //   msg = "${msg} - version: ${version}"
    // }
    if (title == "") {
        title = "<font color=\"info\">流水线成功了</font>"
    } else if (title == "上线啦") {
        msg = "${msg} 🎉🎊🎈"
    }
    title = "${project}：${version}"

    msg = genNotificationMessage(msg, title)
    def buttons = getButtonLinks(project)
    msg = "${msg}${buttons}"
    // new Ding().markDown(title, msg, false, credentialsId)
    try {
        if (credentialsId == "dingTalk"){
            new DingTalk().markDown(title, msg, false, credentialsId)
        }else {
            new WeChat().markDown(credentialsId, msg, true)
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
def notificationFailed(project, title="", version="",  credentialsId="wechatBot") {
    // msg = "查看Jenkins流水线历史记录"
    msg = "<font color=\"warning\">🛑 ${title} 🛑</font>"
    if (title == "") {
        title = "<font color=\"warning\">流水线失败了！</font>"
    }
    title = "${project}：${version}"
    msg = genNotificationMessage(msg, title)
    def buttons = getButtonLinks(project)
    msg = "${msg}${buttons}"
    // new Ding().markDown(title, msg, false, credentialsId)
    try {
        if (credentialsId == "dingTalk"){
            new DingTalk().markDown(title, msg, false, credentialsId)
        }else {
            new WeChat().markDown(credentialsId, msg, true)
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
    msg = "${msg}  \n  **Build Tasks**: ${env.BUILD_RESULT}"
    return msg
}

def genButtons(project="") {
    buttons = [
            [
                    "title": "查看流水线",
                    "actionURL": "${env.RUN_DISPLAY_URL}"
            ],
            [
                    "title": "查看代码扫描",
                    "actionURL": "http://sonar-test.ctyuncdn.cn/dashboard?id=${project}"
            ]
    ]
    if (env.CHANGE_URL != null && env.CHANGE_URL != "") {
        buttons.add([
                "title": "查看PR",
                "actionURL": "${env.CHANGE_URL}"
        ])
    }
    if (env.UNIT_TEST != null && env.UNIT_TEST == "true") {
        buttons.add([
                "title": "查看单元测试结果",
                "actionURL": "${env.BUILD_URL}artifact/artifacts/unit_test.log"
        ])
    }
    return buttons
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

static def updateBuildTasks(String source = "abv", String add) {
    return new Utils().updateBuildMessage(source, add)
}