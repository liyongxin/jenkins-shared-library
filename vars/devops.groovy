#!/usr/bin/env groovy

import cn.ctyun.devops.Build
import cn.ctyun.devops.Deploy


static def dockerBuild(String dockerfile = "Dockerfile", String context = ".", String address = "harbor.ctyuncdn.cn/devops", String tag = "latest", String credentialsId = "") {
    return new Build().build(dockerfile, context, address, tag, credentialsId)
}

static def deploy(String dir = "deploy") {
    return new Deploy().deploy(dir)
}