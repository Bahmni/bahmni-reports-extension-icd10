FROM amazoncorretto:8

ENV SERVER_OPTS="-Xms512m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=512m"
ENV DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,address=8003,server=y,suspend=n"

RUN yum install -y git
RUN yum install -y maven

RUN mvn clean package

COPY target/extensions-1.0-SNAPSHOT.jar /extensions-1.0-SNAPSHOT.jar

CMD ["java", "-jar", "/extensions-1.0-SNAPSHOT.jar"]