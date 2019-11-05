package cn.ctyun.devops

def updateBuildMessage(String source, String add) {
    env.BUILD_RESULT = source + add + "\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    return env.BUILD_RESULT
}