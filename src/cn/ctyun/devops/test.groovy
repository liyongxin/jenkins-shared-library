
// //aaa = sh(returnStdout: true, script: "git log --pretty=format:'' --name-only  -1").trim()
// class Example {
//    static void main(String[] args) {
//       // Using a simple println statement to print output to the console
//       println('Hello World');
//       //def output = sh returnStdout: true, script: 'ls -l'
//       //println(output)
//       this.test()
//       //def result = readFile('commandResult').trim()
//       //println(result)
//    }
//    def test() {
//       sh "git log --pretty=format:'' --name-only  -1|grep jenkins > tmpResult"
//    }
// }


class Test2 {
    public Test2() {
        println "TEST2"
    }
    static void main(String[] args) {
       println('Hello World');
        new Test1()
    }
}
class Test1 {
    public Test1() {
        println "TEST1"
        sh("git log --pretty=format:'' --name-only  -1|grep jenkins > tmpResult")
    }
}
