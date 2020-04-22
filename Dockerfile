from ubuntu:rolling

RUN apt update \
  && apt upgrade -y \
  && apt install -y openjdk-8-jdk ant ca-certificates-java man-db manpages git wget curl unzip zip \
  && ln -sf /bin/bash /bin/sh

ENV LANG en-US
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN git clone https://github.com/3919/snake-server /opt/snake-server \ 
  && wget https://services.gradle.org/distributions/gradle-5.2.1-bin.zip -P /tmp \
  && unzip -d /opt/gradle /tmp/gradle-*.zip

ENV GRADLE_HOME /opt/gradle/gradle-5.2.1
ENV PATH "${GRADLE_HOME}/bin:${PATH}"

RUN cd /opt \
  && wget https://apache.mirrors.tworzy.net/tomee/tomee-8.0.1/apache-tomee-8.0.1-plume.tar.gz -O tomee.tar.gz \
  && ls -la \
  && tar -xf tomee.tar.gz

ENV TOMEEDIR "/opt/apache-tomee-plume-8.0.1/"

RUN cd /opt/snake-server \
  && gradle war \
  && cp build/libs/snake-server-1.0-SNAPSHOT.war $TOMEEDIR/webapps/snake-server.war

COPY entrypoint.sh /usr/bin/snake-server
ENTRYPOINT [ "/usr/bin/snake-server" ]
