FROM adoptopenjdk/openjdk11:latest
ENV JAVA_OPTS="-Xmx256m"
ADD target/payment-ws-*.jar executable.jar
ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar executable.jar