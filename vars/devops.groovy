#!/usr/bin/env groovy
/**
 * @author: liyongxin
 * @Date: 2019-10-14
 */

import com.luffycity.devops.Hello
import com.luffycity.devops.Build
import com.luffycity.devops.DeploySpec
import com.luffycity.devops.DeployMulti
import com.luffycity.devops.Notification
import com.luffycity.devops.Utils


/**
 * @description, for docker image build and push
 * @param dockerfile, path of dockerfile
 * @param context, build context
 * @param address, image repo without tag
 * @param tag, image tag will be builded
 * @param credentialsId, jenkins credential id for docker registry loggin
 * @param args, build args
 * */
def dockerBuild(String dockerfile, String context, String address, String tag, String credentialsId, String args=""){
    return new Build().build(dockerfile, context, address, tag, credentialsId, args)
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
static def deploySpec(String resourcePath, String controllerFilePath, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    return new DeploySpec().deploy(resourcePath, controllerFilePath, watch, timeoutMinutes, sleepTime, kind)
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
static def deployMulti(String resourcePath, String controllerFilePath, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    return new DeployMulti().deploy(resourcePath, controllerFilePath, watch, timeoutMinutes, sleepTime, kind)
}

/**
 * update build task
 * @param source
 * @param add
 */
static def updateBuildTasks(String source = "", String add) {
    return new Utils().updateBuildMessage(source, add)
}

/**
 * notification 
 * @param project
 * @param title
 * @param version
 * @param credentialsId
 * @return Notification object
 */
def notification(project, title="", version="", credentialsId="") {
    return new Notification(project, title, version, credentialsId)
}

/**
 * for demo
 */
def hello(String content) {
    return new Hello().hello(content)
}