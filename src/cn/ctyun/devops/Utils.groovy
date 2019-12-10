package cn.ctyun.devops

def updateBuildMessage(String source, String add) {
    //env.BUILD_RESULT = source + add + "\n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    env.BUILD_RESULT = source + add + "\n                    \n"
    return env.BUILD_RESULT
}