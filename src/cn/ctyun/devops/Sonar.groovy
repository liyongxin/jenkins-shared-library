
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
    if(this.waitScan){
        timeout(60) {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "未通过Sonarqube的代码质量阈检查，请及时修改！failure: ${qg.status}"
            }
        }
    }
    return this
}