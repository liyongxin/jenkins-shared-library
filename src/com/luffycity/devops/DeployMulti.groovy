package com.luffycity.devops

/**
 * @author: YongxinLi
 * @mail: inspur_lyx@hotmail.com
 * @Date: 2020-04-13
 */

import groovy.time.TimeCategory
import org.yaml.snakeyaml.Yaml
import groovy.json.JsonSlurperClassic

/**
 *
 * @param resourcePath: resource file path, dir or file
 * @param controllerFilePath: controller file path, such as deployment
 * @param watch: weather watch resource creation processing
 * @param timeoutMinutes
 * @param sleepTime
 * @param kind
 * @return
 */
def deploy(String resourcePath="deploy", String controllerFilePath = "deploy/depoly.yaml",  Boolean watch = true, int timeoutMinutes = 3, int sleepTime = 5, String kind = "deployment") {
    this.controllerFilePath = controllerFilePath
    this.resourcePath = resourcePath
    if (resourcePath == "" && controllerFilePath != "") {
        echo "resourcePath param is empty, will use controllerFilePath as deploy target"
        this.resourcePath = controllerFilePath
    }
    if(!this.resourcePath && !this.controllerFilePath){
        throw Exception("illegal resource path")
    }
    this.watch = watch
    if (env.TAG_NAME) {
        this.imageTag = env.TAG_NAME
    }
    this.timeoutMinutes = timeoutMinutes
    this.sleepTime = sleepTime
    this.kind = kind
    this.util = new Utils()
    return this
}


def start() {
    try {
        this.tplHandler()
        sh "kubectl apply -f ${this.resourcePath}"
    } catch (Exception exc) {
        updateGitlabCommitStatus(name: 'deploy', state: 'failed')
        this.util.updateBuildMessage(env.BUILD_TASKS, "Service Deploy Failed...  ×")
        echo "failed to deploy,exception: ${exc}."
        throw exc
    }
    // watch workload
    if (this.watch) {
        initK8sProperities()
        echo "begin watch ${this.kind}... ${this.controllerName}"
        String namespace = this.controllerNamespace
        String name = this.controllerName
        int timeoutMinutes = this.timeoutMinutes
        int sleepTime = this.sleepTime
        String kind = this.kind
        monitorDeployment(namespace, name, timeoutMinutes, sleepTime, kind)
    }
    return this
}


def initK8sProperities() {
    try {
        def content = readFile this.controllerFilePath
        Yaml parser = new Yaml()
        def data = parser.load(content)
        def kind = data["kind"]
        if (kind == null || kind == "" || kind.toLowerCase() != this.kind) {
            throw "wrong controller file,expected ${this.kind},actually value is ${kind}"
        }
        echo "${data}"
        this.controllerNamespace = data["metadata"]["namespace"]
        if (!this.controllerNamespace){
            this.controllerNamespace = "default"
        }
        this.controllerName = data["metadata"]["name"]
    } catch (Exception exc) {
        echo "failed to readFile ${this.controllerFilePath},exception: ${exc}."
        throw exc
    }
}



def tplHandler() {
    String namespace = ""
    // set default branch
    if(!env.DEV_BRANCH){
        env.DEV_BRANCH = "develop"
    }
    if(env.BRANCH_NAME ==~ env.DEV_BRANCH){
        namespace = "demo"
    }else if(env.TAG_NAME && env.BRANCH_NAME.matches(/v.*/)){
        namespace = "qa"
    }else{
        throw new Exception("branch check not pass...")
    }
    def targetPath = "${this.resourcePath}/*"
    if(this.resourcePath == this.controllerFilePath){
        targetPath = this.controllerFilePath
    }
    //replace IMAGE_REPO
    sh "sed -i 's#{{IMAGE_URL}}#${env.FULL_IMAGE_ADDRESS}#g' ${targetPath}"

    
    //namespace replace
     sh "sed -i 's#{{NAMESPACE}}#${namespace}#g' ${targetPath}"
    
    // other tpls replace
    def tpls = [
        "NODE_LABEL_KEY",
        "NODE_LABEL_VAL",
        "INGRESS_MYBLOG"
    ]
    def configMapData = this.getResource(namespace, "myblog", "configmap")["data"]
   
    for (key in tpls){
        def val = configMapData[key]
        echo "key is ${key}, val is ${val}"
        sh "sed -i 's#{{${key}}}#${val}#g' ${targetPath}"
    }
}


def delete() {
    try {
        this.tplHandler()
        sh "kubectl delete -f ${this.resourcePath}"
    } catch (Exception exc) {
        echo "failed to delete resource,exception: ${exc}."
        throw exc
    }
    return this
}

def monitorDeployment(String namespace, String name, int timeoutMinutes = 3, sleepTime = 5, String kind = "deployment") {
    def readyCount = 0
    def readyTarget = 5
    use( TimeCategory ) {
        def endTime = TimeCategory.plus(new Date(), TimeCategory.getMinutes(timeoutMinutes))
        def lastRolling
        while (true) {
            // checking timeout
            if (new Date() >= endTime) {
                echo "timeout, printing logs..."
                this.printContainerLogs(lastRolling)
                updateGitlabCommitStatus(name: 'deploy', state: 'failed')
                this.util.updateBuildMessage(env.BUILD_TASKS, "Service Deploy Failed...  ×")
                throw new Exception("deployment timed out...")
            }
            // checking deployment status
            try {
                def rolling = this.getResource(namespace, name, kind)
                sh "kubectl get pod -n ${namespace} -o wide"
                lastRolling = rolling
                if (this.isDeploymentReady(rolling)) {
                    readyCount++
                    echo "ready total count: ${readyCount}"
                    if (readyCount >= readyTarget) {
                        updateGitlabCommitStatus(name: 'deploy', state: 'success')
                        this.util.updateBuildMessage(env.BUILD_TASKS, "Service Deploy OK...  √")
                        break
                    }

                } else {
                    readyCount = 0
                    sh "kubectl get pod -n ${namespace} -o wide"
                }
            } catch (Exception exc) {
                updateGitlabCommitStatus(name: 'deploy', state: 'failed')
                this.util.updateBuildMessage(env.BUILD_TASKS, "Service Deploy Failed...  ×")
                echo "error: ${exc}"
            }
            sleep(sleepTime)
        }
    }
    return this
}

def getResource(String namespace = "default", String name, String kind="deployment") {
    sh "kubectl get ${kind} -n ${namespace} ${name} -o json > ${namespace}-${name}-yaml.yml"
    def jsonStr = readFile "${namespace}-${name}-yaml.yml"
    def jsonSlurper = new JsonSlurperClassic()
    def jsonObj = jsonSlurper.parseText(jsonStr)
    return jsonObj
}


def printContainerLogs(deployJson) {
    if (deployJson == null) {
        return;
    }
    def namespace = deployJson.metadata.namespace
    def name = deployJson.metadata.name
    def labels=""
    deployJson.spec.template.metadata.labels.each { k, v ->
        labels = "${labels} -l=${k}=${v}"
    }
    sh "kubectl describe pods -n ${namespace} ${labels}"
}

def isDeploymentReady(deployJson) {
    def status = deployJson.status
    def replicas = status.replicas
    def unavailable = status['unavailableReplicas']
    def ready = status['readyReplicas']
    if (unavailable != null) {
        return false
    }
    def deployReady = (ready != null && ready == replicas)
    // get pod information
    if (deployJson.spec.template.metadata != null && deployReady) {
        if (deployJson.spec.template.metadata.labels != null) {
            def labels=""
            def namespace = deployJson.metadata.namespace
            def name = deployJson.metadata.name
            deployJson.spec.template.metadata.labels.each { k, v ->
                labels = "${labels} -l=${k}=${v}"
            }
            if (labels != "") {
                sh "kubectl get pods -n ${namespace} ${labels} -o json > ${namespace}-${name}-json.json"
                def jsonStr = readFile "${namespace}-${name}-json.json"
                def jsonSlurper = new JsonSlurperClassic()
                def jsonObj = jsonSlurper.parseText(jsonStr)
                def totalCount = 0
                def readyCount = 0
                jsonObj.items.each { k, v ->
                    echo "pod phase ${k.status.phase}"
                    if (k.status.phase != "Terminating" && k.status.phase != "Evicted") {
                        totalCount++;
                        if (k.status.phase == "Running") {
                            readyCount++;
                        }
                    }
                }
                echo "Pod running count ${totalCount} == ${readyCount}"
                return totalCount > 0 && totalCount == readyCount
            }
        }
    }
    return deployReady
}
