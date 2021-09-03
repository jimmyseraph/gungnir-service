# About Gungnir
![GitHub CI](https://github.com/jimmyseraph/gungnir-service/actions/workflows/maven-ci/badge.svg)
Gungnir is a project to monitor the coverage of the target java project. This just like Jacoco, but the coverage is watched in runtime, not in the end of the test.\
So, you can just remark this is a runtime coverage monitor.

# How to use
This is a project based on Spring Cloud. You can run every micro-service just like a normal Spring Cloud Project.
+ Project structure
  + agent —— standalone java agent.
  + config —— spring cloud config server module.
  + cover —— spring cloud micro-service, core module.
  + discovery —— spring cloud eureka server module.
  + gateway —— spring cloud gateway server module.
  + user —— spring cloud micro-server, authorize module.
  + xxl-job-admin —— standalone spring web app, manage the schedule job.

## Deploy DB
+ MongoDB
```shell
docker network create mongo-net
docker run -d --network mongo-net --name dev_mongo -e MONGO_INITDB_ROOT_USERNAME=mongoadmin -e MONGO_INITDB_ROOT_PASSWORD=Aa-123456 -p 27017:27017 mongo:latest
# connect to mongo
docker run -it --rm --network mongo-net mongo mongo --host dev_mongo -u mongoadmin -p Aa-123456 --authenticationDatabase admin
# create db
use gungnir
# create user
db.createUser(
    {
        user: "gungnirUser",
        pwd: "Gungnir-123456",
        roles: [
            { role: "readWrite", db: "gungnir"}
        ]
    }
)
```

+ (Optional) MongoDB-Express (Admin Web-based)
```shell
docker run -d --network mongo-net  --name mongo_web -e ME_CONFIG_MONGODB_SERVER=dev_mongo -e ME_CONFIG_MONGODB_ADMINUSERNAME=mongoadmin -e ME_CONFIG_MONGODB_ADMINPASSWORD=Aa-123456 -p 8081:8081 mongo-express:latest
```
+ MySQL 8.0
```shell
docker run --name gungnir-mysql -e MYSQL_ROOT_PASSWORD=Gungnir123! -p3306:3306  -d mysql:8.0
# initial db with sql script in `script/db/sql/tables_xxl_jobs.sql`
```

## Config the project
This project based on spring-cloud-config on git. So you can set up a repository on GitHub/GitLab etc. The demo config profiles are in  [config repository](https://github.com/jimmyseraph/gungnir-config). \
You can config different env-profiles by adding the special env-name as suffix like "-local" / "-dev" / "-test". \
More information refer to [Spring Cloud Config Doc](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/).

+ Config cover-service
```yaml
upload.path: /your/path/to/save/upload_file
repository.base-dir: /your/path/to/save/repository
socket.port: 6300 # gungnir socket server port.
job.pool-num: 10 # thread pool to do the backend job.
job.wait-for-timeouts: 30 # minutes of the max timeout duration.
```

+ Config user-service
```yaml
token.expire: 864000 # token expired time in seconds.
jwt.key: jwt-secret-key
```

## Gungnir Agent
gungnir-agent is an agent developed base on jacoco-agent.
+ Get agent \
You can get agent from release or compile the agent project yourself as below:
```shell
cd agent
mvn package
```
You will find the jar file with `jar-with-dependencies` suffix in target directory.

+ Run agent with your project \
Target project should run with gungnir-agent, like this:
```shell
java -javaagent:/your/path/to/gungnir-agent.jar=output=tcpclient,address=${ip-address},port=6300,projectname=${project_name} -jar target_project.jar
```
More parameters please see [JaCoCo Agent](https://www.jacoco.org/jacoco/trunk/doc/agent.html).
> Notice: project_name must be same as what you set in the Gungnir-Website

## Others
+ Access to git repository \
Before start config service, make sure the `id_rsa` which your private key to access GitHub/GitLab repository is available in `/{HOME}/.ssh` directory.

+ Port \
Make sure the default ports 6300, 10001-10004, 20000 are free to use.

+ xxl-job-admin \
You should first start xxl-job-admin before start other services.
