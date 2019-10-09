@Grapes(
        @Grab(group='org.yaml', module='snakeyaml', version='1.17')
)

package cn.ctyun.devops

import groovy.time.TimeCategory
import cn.ctyun.devops.YamlFile
import org.yaml.snakeyaml.Yaml

def deploy(String dir, String resourceYaml, Boolean watch = true, int timeoutMinutes = 5, int sleepTime = 5, String kind = "deployment") {
    this.resourceYaml = resourceYaml
    this.target = dir
    if (dir == "" && resourceYaml != "") {
        echo "dir param is empty, will use resourceYaml as deploy target"
        this.target = resourceYaml
    }
    this.watch = watch
    this.timeoutMinutes = timeoutMinutes
    this.sleepTime = sleepTime
    this.kind = kind
    //todo for more param support
    return this
}

def start() {
    try {
        sh "kubectl apply -f ${this.target}"
    } catch (Exception exc) {
        echo "failed to deploy,exception: ${exc}."
        throw exc
    }
    if (this.watch) {
        echo "begin watch ${this.kind}..."
        def filePath = "deploy/deploy.yaml"
        def fileContent = readFile filePath
        def yaml = new Yaml()
        def content = yaml.load(fileContent)
        echo "${content}"
    }
    return this
}

def delete() {
    try {
        sh "kubectl delete -f ${this.target}"
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