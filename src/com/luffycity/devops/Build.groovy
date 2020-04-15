package com.luffycity.devops;

/**
 * @author: YongxinLi
 * @mail: inspur_lyx@hotmail.com
 * @Date: 2020-04-13
 * */

/**
 * @param dockerfile, path of dockerfile
 * @param context, build context
 * @param address, image repo without tag
 * @param tag, image tag will be builded
 * @param credentialsId, jenkins credential id for docker registry loggin
 * @param args, build args
 * */
def build(String dockerfile, String context, String address, String tag, String credentialsId, String args="") {
    this.dockerfile = dockerfile
    this.context = context
    this.address = address
    this.tag = tag
    this.credentialsId = credentialsId
    this.ready = false
    this.args = args
    this.isLoggedIn = false
    this.util = new Utils()
    return this
}

def start() {
    def FULL_ADDRESS = "${this.address}:${this.tag}"
    if (this.args != "") {
        echo "build args: ${this.args}"
    }
    this.login()
    try {
        sh "docker build -t ${FULL_ADDRESS} -f ${this.dockerfile} ${this.args} ${this.context}"
        updateGitlabCommitStatus(name: 'image-build', state: 'success')
        this.util.updateBuildMessage(env.BUILD_TASKS, "Image Build OK...  √")
    }catch (Exception ignored) {
        updateGitlabCommitStatus(name: 'image-build', state: 'failed')
        this.util.updateBuildMessage(env.BUILD_TASKS, "Image Build Failed...  ×")
    }
    return this
}

def login() {
    if (this.isLoggedIn || this.credentialsId == "") {
        return this;
    }
    withCredentials([usernamePassword(credentialsId: this.credentialsId, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        def regs = this.getRegistry()
        retry(3) {
            try {
                sh "docker login ${regs} -u $USERNAME -p $PASSWORD"
            } catch (Exception exc) {
                updateGitlabCommitStatus(name: 'image-build', state: 'failed')
                updateGitlabCommitStatus(name: 'docker-login', state: 'failed')
                new Utils().updateBuildMessage(env.BUILD_TASKS, "Image Build Failed...  ×")
                throw exc
            }
        }
    }
    this.isLoggedIn = true;
    return this;
}

def getRegistry() {
    def sp = this.address.split("/")
    if (sp.size() > 1) {
        return sp[0]
    }
    return this.address
}

def push() {
    def tag = env.TAG_NAME
    if (tag == "" || !tag) {
        tag = this.tag
    }
    def FULL_ADDRESS = "${this.address}:${tag}"
    def ORIG_ADDRESS = "${this.address}:${this.tag}"
    this.login()
    retry(3) {
        try {
            sh "docker push ${ORIG_ADDRESS}"
            if(tag != "" && tag != this.tag){
                sh "docker tag ${ORIG_ADDRESS} ${FULL_ADDRESS}"
                echo "commit with tag ${tag}, will push ${FULL_ADDRESS}"
                sh "docker push ${FULL_ADDRESS}"
            }
        } catch (Exception exc) {
            echo "error: ${exc}.. "
            updateGitlabCommitStatus(name: 'image-build', state: 'failed')
            new Utils().updateBuildMessage(env.BUILD_TASKS, "Image Build Failed...  ×")
            throw exc
        }
    }
    updateGitlabCommitStatus(name: 'image-push', state: 'success')
    new Utils().updateBuildMessage(env.BUILD_TASKS, "Image Push OK...  √")
    env.FULL_IMAGE_ADDRESS = FULL_ADDRESS
    return this
}


def getImage(String tag = "") {
    if (tag == "") {
        tag = this.tag
    }
    return "${this.address}:${tag}"
}