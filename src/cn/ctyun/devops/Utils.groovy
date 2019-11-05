package cn.ctyun.devops

def updateBuildMessage(String source, String add) {
    env.BUILD_RESULT = source + add + "\n      "
    return env.BUILD_RESULT
}