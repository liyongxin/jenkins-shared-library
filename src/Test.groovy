
import org.yaml.snakeyaml.Yaml

Yaml parser = new Yaml()
HashMap example = parser.load(("ingress.yaml" as File).text)

//example.each{println it.subject}