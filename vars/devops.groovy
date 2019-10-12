#!/usr/bin/env groovy

import cn.ctyun.devops.Build
import cn.ctyun.devops.Deploy
import cn.ctyun.devops.Sonar

static def dockerBuild(String dockerfile = "Dockerfile", String context = ".", String address = "harbor.ctyuncdn.cn/devops", String tag = "latest", String credentialsId = "") {
    return new Build().build(dockerfile, context, address, tag, credentialsId)
}

static def deploy(String resourcePath, String controllerFilePath, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    return new Deploy().deploy(resourcePath, controllerFilePath, watch, timeoutMinutes, sleepTime, kind)
}

static def scan(Boolean debug = true, Boolean waitScan = true, Boolean interupt = true) {
    return new Sonar().scan(debug, waitScan, interupt)
}