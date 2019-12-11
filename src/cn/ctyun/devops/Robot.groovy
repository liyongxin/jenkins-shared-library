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
        echo rf.getResult()
        echo rf.number
        echo rf.absoluteUrl
        new Utils().updateBuildMessage(env.BUILD_RESULT, "Acceptance Test...  √")
    } catch (Exception exc) {
        echo "trigger  execute Acceptance Testing exception: ${exc}"

        new Utils().updateBuildMessage(env.BUILD_RESULT, "Acceptance Test...  ×")
    }
}