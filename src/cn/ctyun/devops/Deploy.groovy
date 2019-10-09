package cn.ctyun.devops

import groovy.time.TimeCategory
import org.yaml.snakeyaml.Yaml

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
def deploy(String resourcePath="deploy", String controllerFilePath = "deploy/depoly.yaml", Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    this.controllerFilePath = controllerFilePath
    this.resourcePath = resourcePath
    if (resourcePath == "" && controllerFilePath != "") {
        echo "resourcePath param is empty, will use controllerFilePath as deploy target"
        this.resourcePath = controllerFilePath
    }
    this.watch = watch
    this.timeoutMinutes = timeoutMinutes
    this.sleepTime = sleepTime
    this.kind = kind
    return this
}

def start() {
    try {
        sh "kubectl apply -f ${this.resourcePath}"
    } catch (Exception exc) {
        echo "failed to deploy,exception: ${exc}."
        throw exc
    }
    if (this.watch) {
        echo "begin watch ${this.kind}..."
        sh "pwd"
        sh "ls -al"
        sh "ls ${this.controllerFilePath}"
        Yaml parser = new Yaml()
        HashMap content = parser.load((this.controllerFilePath as File).text)
        echo "${content}"
        echo "${content["kind"]}"
    }
    return this
}

def delete() {
    try {
        sh "kubectl delete -f ${this.resourcePath}"
    } catch (Exception exc) {
        echo "failed to delete resource,exception: ${exc}."
        throw exc
    }
    return this
}

def monitorDeployment(String namespace, String name, int timeoutMinutes = 10, sleepTime = 2, String kind = "deployment") {
    def readyCount = 0
    def readyTarget = 3
    use( TimeCategory ) {
        def endTime = TimeCategory.plus(new Date(), TimeCategory.getMinutes(timeoutMinutes))
        def lastRolling
        while (true) {
            // checking timeout
            if (new Date() >= endTime) {
                echo "timeout, printing logs..."
                this.printContainerLogs(lastRolling)
                throw new Exception("deployment timed out...")
            }
            // checking deployment status
            try {
                def rolling = this.getDeployment(namespace, name, kind)
                lastRolling = rolling
                if (this.isDeploymentReady(rolling)) {

                    readyCount++
                    echo "ready total count: ${readyCount}"
                    if (readyCount >= readyTarget) {
                        break
                    }

                } else {
                    readyCount = 0
                    echo "reseting ready total count: ${readyCount}"
                    sh "kubectl get pod -n ${namespace} -o wide"
                }
            } catch (Exception exc) {
                echo "error: ${exc}"
            }
            sleep(sleepTime)
        }
    }
}