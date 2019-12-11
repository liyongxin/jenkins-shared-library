package cn.ctyun.devops

/**
 * @author: liyongxin
 * @Date: 2019-20-11
 */

def acceptanceTest(comp="") {
    try{
        echo "Trigger to execute Acceptance Testing"
        build job: 'rf_UI',
                parameters: [
                    string(name: 'comp', value: '${comp}')
                ],
                wait: true
        //propagate: false
        new Utils().updateBuildMessage(env.BUILD_RESULT, "Acceptance Test...  √")
    } catch (Exception exc) {
        echo "trigger  execute Acceptance Testing exception: ${exc}"
        new Utils().updateBuildMessage(env.BUILD_RESULT, "Acceptance Test...  ×")
    }
}