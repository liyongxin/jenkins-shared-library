
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
HashMap example = parser.load(("ingress.yaml" as File).text)
println(example["kind"])
//example.each{println it.subject}