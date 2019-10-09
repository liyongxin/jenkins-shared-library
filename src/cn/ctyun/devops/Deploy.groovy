package cn.ctyun.devops

def deploy(String dir) {
    try {
        sh "kubectl apply -f ${dir}"
    } catch (Exception exc) {
        echo "failed to deploy,exception: ${exc}."
    }
}