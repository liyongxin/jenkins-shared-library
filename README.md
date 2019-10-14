# jenkins shared library 

## 描述

本项目为Jenkins的共享库，封装并实现了项目在CICD过程中所需的通用功能，使用groovy实现。

### 目录说明
 - `src/cn.ctyun.devops`: 通用功能所在的package包
 - `src/org.yaml.snakeyaml`: 解析yaml所用的第三方库
 - `vars/devops.groovy`: 对外方法入口文件，所有Jenkins可调用的入口方法均在此定义

## 功能说明

### Build
提供`docker image`的构建功能，参数说明：
 - `dockerfile`: Dockerfile的路径，可以配置相对路径，相对于项目根目录
 - `context`: 执行构建的上下文路径，为保证镜像尽可能简化，该目录下只放构建必须的文件
 - `address`: 构建镜像的地址，比如`harbor.ctyuncdn.cn/devops/jenkins/test-jenkins-build`
 - `tag`: 构建镜像的tag，和address一起拼接出最终的镜像地址
 - `credentialsId`: 配置的镜像仓库的访问凭据，用于将构建的镜像推送至镜像仓库

### Deploy
提供服务的部署功能，参数说明：
 - `resourcePath`: 要部署到`kubernetes`中的资源文件路径，使用相对路径，可以是单个文件，也可以是一个包含了各种`kubernetes`资源文件的目录，这些资源文件包含deployment、service、configmap、ingress等
 - `controllerFilePath`: 控制器的文件路径，必须为文件路径，目前支持`deployment`，此处传递`deploy.yaml`的文件相对路径即可
 - `watch`: 布尔类型，是否watch控制器资源文件(`deploy.yaml`)的创建过程，若watch，则pipeline会等待资源文件创建完成且会对创建结果进行校验
 - `timeoutMinutes`: 配合watch参数来使用，若watch设置为true，则在设置的`timeoutMinutes`时间内观察资源文件是否创建成功，若该时间内pod未能成功，则pipeline进入失败阶段，单位为分钟，默认为3分钟
 - `sleepTime`: 配合watch参数来使用，watch的过程中，每次检测是否资源创建所等待的时间间隔，单位为秒，默认为5秒
 - `kind`: 资源文件类型，默认为`deployment`，目前暂时只支持`deployment`

### Sonar
提供代码扫描功能，使用sonarqube，参数说明：
 - `debug`: 布尔类型，是否开启代码扫描的debug功能
 - `waitScan`: 布尔类型，是否等待扫描完成并检查本次扫描的`Quality Gate`结果
 - `interupt`: 布尔类型，当扫描完成后，若本次扫描的`Quality Gate`结果不是OK，是否中断pipeline，若为true，则中断流水线


## 后续
该项目会根据项目实际使用的情况进行不断完善，如有疑问请联系`liyongxin@chinatelecom.cn`