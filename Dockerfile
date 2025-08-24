#-------------------------------------------------------------------------------
# Copyright (C) 2019 Tarana Wireless, Inc. All Rights Reserved.
#-------------------------------------------------------------------------------
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17

VOLUME /tmp

ADD target/testpire*.jar testpire.jar

EXPOSE 8080
ENTRYPOINT java --add-exports java.base/sun.security.x509=ALL-UNNAMED $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -jar /testpire.jar
