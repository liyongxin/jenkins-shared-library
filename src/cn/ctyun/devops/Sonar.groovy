
package cn.ctyun.devops

def scan(Boolean debug = true, Boolean waitScan = true) {
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
    def gitbranch = ""
    try {
        gitbranch = env.BRANCH_NAME
        if (gitbranch == null) {
            sh "git branch | grep '*' > gitbranch.file"
            gitbranch = readFile "gitbranch.file"
            gitbranch = gitbranch.replace("*", "").replace(" ", "")
        }
    } catch (Exception exc) {}
    if (gitbranch != null && gitbranch != "") {
        isDebug = "-Dsonar.branch.name=${gitbranch} ${isDebug}"
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
    return this
}