package com.luffycity.devops

def updateBuildMessage(String source, String add) {
    if(!source){
        source = ""
    }
    env.BUILD_RESULT = source + add + "\n                    \n&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    return env.BUILD_RESULT
}