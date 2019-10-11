
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
    sh """
        cd ${this.folder}
        ${scannerCLI} ${this.debug} 
        ls -la .scannerwork
    """
    if (this.folder != ".") {
        sh """
            cp -r ${this.folder}/.scannerwork .
            ls -la .scannerwork
        """
    }
    return this
}