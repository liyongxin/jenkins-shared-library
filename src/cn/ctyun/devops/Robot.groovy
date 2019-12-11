package cn.ctyun.devops

/**
 * @author: liyongxin
 * @Date: 2019-20-11
 */

def acceptanceTest(comp="") {
    try{
        echo "Trigger to execute Acceptance Testing"
        def rf = build job: 'rf_UI',
                        parameters: [
                            string(name: 'comp', value: comp)
                        ],
                        wait: true,
                        propagate: false
        def result = rf.getResult()
        def msg = "Acceptance Test... "
        if (result == "SUCCESS"){
            msg += "√ success"
        }else if(result == "UNSTABLE"){
            msg += "⚠ unstable"
        }else{
            msg += "× failure"
        }
        echo rf.getAbsoluteUrl()
        env.ACCEPT_TEST_URL = echo rf.getAbsoluteUrl()
        new Utils().updateBuildMessage(env.BUILD_RESULT, msg)
    } catch (Exception exc) {
        echo "trigger  execute Acceptance Testing exception: ${exc}"
        new Utils().updateBuildMessage(env.BUILD_RESULT, "Acceptance Test...  ×")
    }
}