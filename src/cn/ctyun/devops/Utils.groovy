package cn.ctyun.devops

def updateBuildMessage(String source, String add) {
    env.BUILD_RESULT = source + add + "\n\t&nbsp;"
    return env.BUILD_RESULT
}