package cn.ctyun.devops

/**
 * @author: liyongxin
 * @Date: 2019-10-10
 */


import groovy.time.TimeCategory
import org.yaml.snakeyaml.Yaml
import groovy.json.JsonOutput
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
def deploy(String resourcePath="deploy", String controllerFilePath = "deploy/depoly.yaml",String imageTag,  Boolean watch = true, int timeoutMinutes = 3, int sleepTime = 5, String kind = "deployment") {
    this.controllerFilePath = controllerFilePath
    this.resourcePath = resourcePath
    if (resourcePath == "" && controllerFilePath != "") {
        echo "resourcePath param is empty, will use controllerFilePath as deploy target"
        this.resourcePath = controllerFilePath
    }
    this.watch = watch
    this.imageTag = imageTag
    //def tag = sh(returnStdout: true, script: "git tag -l --points-at HEAD").split("\n")[0]
    def tag = env.TAG_NAME
    if (tag != "" && tag && tag !="true") {
        echo "env.TAG_NAME is ${tag} "
        this.imageTag = tag
    }
    this.timeoutMinutes = timeoutMinutes
    this.sleepTime = sleepTime
    this.kind = kind
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
        if (null == this.controllerNamespace || "" == this.controllerNamespace){
            this.controllerNamespace = "default"
        }
        this.controllerName = data["metadata"]["name"]
    } catch (Exception exc) {
        echo "failed to readFile ${this.controllerFilePath},exception: ${exc}."
        throw exc
    }
}

def start() {
    try {
        this.tplHandler()
        sh "kubectl apply -f ${this.resourcePath}"
    } catch (Exception exc) {
        updateGitlabCommitStatus(name: 'deploy', state: 'failed')
        new Utils().updateBuildMessage(env.BUILD_RESULT, "Service Deploy Failed...  ×")
        echo "failed to deploy,exception: ${exc}."
        throw exc
    }
    if (this.watch) {
        echo "begin watch ${this.kind}..."
        initK8sProperities()
        //monitorDeployment("aa", "vv")
        String namespace = this.controllerNamespace
        String name = this.controllerName
        int timeoutMinutes = this.timeoutMinutes
        int sleepTime = this.sleepTime
        String kind = this.kind
        monitorDeployment(namespace, name, timeoutMinutes, sleepTime, kind)
    }
    return this
}

def tplHandler() {
    def regBranch = /v.*/
    String namespace = "test"
    // set default branch
    if(!env.DEV_BRANCH){
        env.DEV_BRANCH = "master"
    }
    if(env.BRANCH_NAME ==~ env.DEV_BRANCH){
        namespace = "zcxt-dev"
    }else if(!env.BRANCH_NAME.matches(/v.*/)){
        throw new Exception("branch check not pass...")
    }
    String nodeLabelKeyTpl = "NODE_LABEL_KEY"
    String nodeLabelValTpl = "NODE_LABEL_VAL"
    String ingressBusinessTpl = "INGRESS_BUSINESS"
    String ingressConsoleTpl = "INGRESS_CONSOLE"
    String ingressOperateTpl = "INGRESS_OPERATE"
    def configMapObj = this.getResource(namespace, "cm-zcxt", "configmap")
    def configData = configMapObj["data"]
    String nodeLabelKey = configData[nodeLabelKeyTpl]
    String nodeLabelVal = configData[nodeLabelValTpl]
    String ingressBusiness = configData[ingressBusinessTpl]
    String ingressConsole = configData[ingressConsoleTpl]
    String ingressOperate = configData[ingressOperateTpl]
    echo "nodeLabelKey is " + nodeLabelKey
    echo "nodeLabelVal is " + nodeLabelVal
    echo "ingressBusiness is " + ingressBusiness
    echo "ingressConsole is " + ingressConsole
    echo "ingressOperate is " + ingressOperate
    //namespace
    sh "sed -i 's#{{NAMESPACE}}#${namespace}#g' ${this.resourcePath}/*"
    // imageUrl
    sh "sed -i 's#{{imageUrl}}#${env.IMAGE_REPOSITORY}:${this.imageTag}#g' ${this.resourcePath}/*"
    //nodeLabel
    sh "sed -i 's#{{${nodeLabelKeyTpl}}}#${nodeLabelKey}#g' ${this.resourcePath}/*"
    sh "sed -i 's#{{${nodeLabelValTpl}}}#${nodeLabelVal}#g' ${this.resourcePath}/*"

    //ingress
    sh "sed -i 's#{{${ingressBusinessTpl}}}#${ingressBusiness}#g' ${this.resourcePath}/*"
    sh "sed -i 's#{{${ingressConsoleTpl}}}#${ingressConsole}#g' ${this.resourcePath}/*"
    sh "sed -i 's#{{${ingressOperateTpl}}}#${ingressOperate}#g' ${this.resourcePath}/*"

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
                new Utils().updateBuildMessage(env.BUILD_RESULT, "Service Deploy Failed...  ×")
                throw new Exception("deployment timed out...")
            }
            // checking deployment status
            try {
                def rolling = this.getResource(namespace, name, kind)
                lastRolling = rolling
                if (this.isDeploymentReady(rolling)) {

                    readyCount++
                    echo "ready total count: ${readyCount}"
                    if (readyCount >= readyTarget) {
                        updateGitlabCommitStatus(name: 'deploy', state: 'success')
                        new Utils().updateBuildMessage(env.BUILD_RESULT, "Service Deploy OK...  √")
                        break
                    }

                } else {
                    readyCount = 0
                    echo "reseting ready total count: ${readyCount}"
                    sh "kubectl get pod -n ${namespace} -o wide"
                }
            } catch (Exception exc) {
                updateGitlabCommitStatus(name: 'deploy', state: 'failed')
                new Utils().updateBuildMessage(env.BUILD_RESULT, "Service Deploy Failed...  ×")
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