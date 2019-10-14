#!/usr/bin/env groovy
/**
 * @author: liyongxin
 * @Date: 2019-10-14
 */

import cn.ctyun.devops.Build
import cn.ctyun.devops.Deploy
import cn.ctyun.devops.Sonar

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