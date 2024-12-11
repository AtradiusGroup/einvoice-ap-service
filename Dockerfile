FROM cicdweuprddevopsacr.azurecr.io/sc/common-docker-image:21.1.0

COPY ./target/einvoice-ap-service-*.jar einvoice-ap-service.jar
COPY ./src/main/resources/eintruststore eintruststore

EXPOSE 8080
ENV JAVA_OPTS "-Dhttps.protocols=TLSv1.2 -Djavax.net.ssl.trustStore=/app/eintruststore -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.ssl.trustStoreType=PKCS12"

ENTRYPOINT exec java $JAVA_OPTS -jar /app/einvoice-ap-service.jar
