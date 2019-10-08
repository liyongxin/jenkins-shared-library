#!/usr/bin/env groovy

import cn.ctyun.devops.Build


static def dockerBuild(String dockerfile = "Dockerfile", String context = ".", String address = "harbor.ctyuncdn.cn/devops", String tag = "latest", String credentialsId = "") {
    return new Build().build(dockerfile, context, address, tag, credentialsId)
}
