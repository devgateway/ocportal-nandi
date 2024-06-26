FROM maven:3.8-openjdk-17 as compiler
WORKDIR /tmp/build
RUN microdnf install zip
COPY forms/pom.xml forms/pom.xml
COPY persistence/pom.xml persistence/pom.xml
COPY persistence-mongodb/pom.xml persistence-mongodb/pom.xml
COPY web/pom.xml web/pom.xml
COPY pom.xml .
ENV MAVEN_OPTS="-XX:-TieredCompilation -XX:TieredStopAtLevel=1"
#we run dependency:go-offline on all pom.xml files copied, to get the dependencies first
RUN --mount=type=cache,target=/root/.m2,id=ocnandi-m2 \
  find . -name pom.xml \
  | xargs -I@ mvn -B -f @ dependency:go-offline dependency:resolve-plugins \
  | true
COPY . .
#we compile the code then we explode the fat jar. This is useful to create a reusable layer and save image space/compile time
RUN --mount=type=cache,target=/root/.m2,id=ocnandi-m2 \
  mvn -T 1C clean package -DskipTests -Dmaven.javadoc.skip=true -Dmaven.test.skip=true -Dmaven.gitcommitid.skip=true
RUN mkdir -p forms/target/deps \
    && cd forms/target/deps \
    && unzip -qo '../*.jar' || \
    ( e=$? && if [ $e -ne 1 ]; then exit $e; fi ) \
    && rm -f ../*.*

FROM openjdk:17-jdk-slim as prod
WORKDIR /opt/app
RUN apt-get update && apt-get install -y fontconfig libfreetype6 && rm -rf /var/lib/apt/lists/*
#we copy artifacts from exploded jar, one by one, each COPY command will create a separate docker layer
#this means that for example if lib folder gets unchanged in between builds (no jars were updated) the same layer is reused
COPY --from=compiler /tmp/build/forms/target/deps/BOOT-INF/lib lib
COPY --from=compiler /tmp/build/forms/target/deps/META-INF META-INF
COPY --from=compiler /tmp/build/forms/target/deps/BOOT-INF/classes .
COPY --chmod=0755 entrypoint.sh .
EXPOSE 8090
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.12.0/wait /wait
RUN chmod +x /wait
CMD /wait && /opt/app/entrypoint.sh admin

FROM openjdk:17-jdk-slim as dev
WORKDIR /opt/app
RUN apt-get update && apt-get install -y fontconfig libfreetype6 && rm -rf /var/lib/apt/lists/*
COPY --from=compiler /tmp/build/forms/target/deps/BOOT-INF/lib lib
RUN rm -f lib/persistence*-SNAPSHOT.jar
RUN rm -f lib/web*-SNAPSHOT.jar
COPY --chmod=0755 entrypoint.sh .
EXPOSE 8090
EXPOSE 8000
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=*:8000,server=y,suspend=n
#we use the docker-compose-wait script to wait for port 5432. postgres may be busy importing the db if db is not present
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.12.0/wait /wait
RUN chmod +x /wait
CMD /wait && /opt/app/entrypoint.sh admin-dev