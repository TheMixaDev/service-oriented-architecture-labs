

./asadmin create-jvm-options -Djavax.net.ssl.keyStore=/Users/alwx/Desktop/payara5/bin/keystore.p12
./asadmin create-jvm-options -Djavax.net.ssl.keyStorePassword=1234
./asadmin create-jvm-options -Djavax.net.ssl.keyStoreType=PKCS12

./asadmin restart-domain


./asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.ssl.cert-nickname=s1as
./asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.port=27274

export _JAVA_OPTIONS="-XX:MaxHeapSize=2G -XX:MaxMetaspaceSize=1G"


java -Djavax.net.ssl.keyStore=./keystore.p12 -Djavax.net.ssl.keyStorePassword=1234 -Djavax.net.ssl.keyStoreType=PKCS12 -Djavax.net.ssl.trustStore=./keystore.p12 -Djavax.net.ssl.trustStorePassword=1234 -jar ./payara-micro-5.2022.5.jar --deploy ./navigator-service.war --contextRoot / --port 32412 --sslPort 27274


https://127.0.0.1:27275/ws/routes.wsdl


keytool -genkeypair \
  -alias s1as \
  -keyalg RSA -keysize 2048 \
  -validity 365 \
  -keystore keystore.p12 \
  -storetype PKCS12 \
  -storepass 1234 \
  -dname "CN=localhost, OU=Testing, O=Development, L=Moscow, ST=Moscow, C=RU" \
  -ext "SAN=DNS:localhost,DNS:lab.aeeph.com"

  keytool -exportcert \
  -alias s1as \
  -keystore keystore.p12 \
  -storetype PKCS12 \
  -storepass 1234 \
  -file mule-listener.cer

  keytool -importcert\         
  -alias mule-listener \
  -file mule-listener.cer \
  -keystore navigator-truststore.p12 \
  -storetype PKCS12 \
  -storepass 123456 \
  -noprompt

java \
  -Djavax.net.ssl.keyStore=/Users/alexalex/AnypointStudio/studio-workspace/lab4/src/main/resources/keystore.p12 \
  -Djavax.net.ssl.keyStorePassword=1234 \
  -Djavax.net.ssl.keyStoreType=PKCS12 \
  -Djavax.net.ssl.trustStore=/Users/alexalex/AnypointStudio/studio-workspace/lab4/src/main/resources/navigator-truststore.p12 \
  -Djavax.net.ssl.trustStorePassword=123456 \
  -Djavax.net.ssl.trustStoreType=PKCS12 \
  -jar ./payara-micro-5.2022.5.jar \
  --deploy ./navigator-service.war \
  --contextRoot / \
  --port 32412 \
  --sslPort 27274