package cn.ctyun.devops

def updateBuildMessage(String source, String add) {
    env.BUILD_RESULT = source + add
    return source + add
}