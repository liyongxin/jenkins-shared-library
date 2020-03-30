
import org.yaml.snakeyaml.Yaml

def a = 1
if( a instanceof Character){
    println(2)
}else {
    println(a)
}

println(a.getClass())
def reg = "v.*"
def tag_name = "1v123"
println (tag_name && "1v12.12" ==~ reg)