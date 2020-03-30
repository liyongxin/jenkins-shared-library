
import org.yaml.snakeyaml.Yaml

def a = 1
if( a instanceof Character){
    println(2)
}else {
    println(a)
}

println(a.getClass())

println ("1v12.12" ==~ /v.*/)