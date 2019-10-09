package cn.ctyun.devops

def deploy(String dir) {
    this.dir = dir
    //todo for more param support
}

def start() {
    try {
        sh "kubectl apply -f ${this.dir}"
    } catch (Exception exc) {
        echo "failed to deploy,exception: ${exc}."
    }
}