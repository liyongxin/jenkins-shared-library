
package cn.ctyun.devops

def scan(Boolean debug = true, Boolean waitScan = true) {
    this.folder = "."
    this.debug = debug
    this.waitScan = waitScan
    return this
}

def start(install=false) {
    try {
        this.startToSonar(install)
    }
    catch (Exception exc) {
        echo "error scan sonar: ${exc}"
    }
    return this
}

def startToSonar(install=true) {
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
    //wait 3min
    timeout(time: 3, unit: 'MINUTES') {
        def qg = waitForQualityGate()
        echo "Status first: ${qg.status}"
        if (qg.status != 'Error') {
            echo "Status: ${qg.status}"
            error "Pipeline aborted due to quality gate failure: ${qg.status}"
        }
    }
    return this
}