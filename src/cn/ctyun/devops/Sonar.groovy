package cn.ctyun.devops

/**
 * @author: liyongxin
 * @Date: 2019-10-10
 */


def scan(String projectVersion="", Boolean debug = true, Boolean waitScan = true, Boolean interupt = false) {
    this.folder = "."
    this.debug = debug
    this.waitScan = waitScan
    this.interupt = interupt
    if (projectVersion == ""){
        projectVersion = sh(returnStdout: true, script: 'git log --oneline -n 1|cut -d " " -f 1')
    }
    sh "echo '\nsonar.projectVersion=${projectVersion}' >> sonar-project.properties"
    sh "cat sonar-project.properties"
    return this
}

def start(install=false) {
    try {
        sh "cat /etc/hosts"
        this.startToSonar(install)
    }
    catch (Exception exc) {
        if(this.interupt){
            throw exc
        }else{
            echo "error scan sonar: ${exc}"
        }
    }
    return this
}

def startToSonar(install=false) {
    withSonarQubeEnv('sonarqube') {
        def scannerCLI = "sonar-scanner";
        if (install) {
            def scannerHome = tool 'sonarqube';
            scannerCLI = "${scannerHome}/bin/sonar-scanner"
            sh "chmod +x ${scannerHome}/bin/sonar-scanner || true"
        }
        def isDebug = ""
        if (this.debug) {
            isDebug = " -X "
        }
        sh """
            cd ${this.folder}
            ${scannerCLI} ${isDebug} 
            ls -la .scannerwork
        """
        if (this.folder != ".") {
            sh """
                cp -r ${this.folder}/.scannerwork .
                ls -la .scannerwork
            """
        }
    }
    if(this.waitScan){
        //wait 3min
        timeout(time: 3, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
                env.BUILD_RESULT += "Code Scan OK|"
                updateGitlabCommitStatus(name: 'SonarQube analysis', state: 'failed')
            }else{
                env.BUILD_RESULT += "Code Scan Failed|"
                updateGitlabCommitStatus(name: 'SonarQube analysis', state: 'success')
            }
        }
    }else{
        echo "skip waitScan"
    }
    return this
}