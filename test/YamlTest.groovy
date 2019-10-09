
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
HashMap example = parser.load(("./ingress.yaml" as File).text)
println(example["kind"])
//example.each{println it.subject}
def path = "/ingress.yaml"
def fileContent = readFile(path)
def yaml = new Yaml()
def content = yaml.load(fileContent)
println(content["kind"])