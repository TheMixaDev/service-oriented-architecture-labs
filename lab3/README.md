

./asadmin create-jvm-options -Djavax.net.ssl.keyStore=/Users/alwx/Desktop/payara5/bin/keystore.p12
./asadmin create-jvm-options -Djavax.net.ssl.keyStorePassword=1234
./asadmin create-jvm-options -Djavax.net.ssl.keyStoreType=PKCS12

./asadmin restart-domain


./asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.ssl.cert-nickname=s1as
./asadmin set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.port=27274

export _JAVA_OPTIONS="-XX:MaxHeapSize=2G -XX:MaxMetaspaceSize=1G"


java -Djavax.net.ssl.keyStore=./keystore.p12 -Djavax.net.ssl.keyStorePassword=1234 -Djavax.net.ssl.keyStoreType=PKCS12 -Djavax.net.ssl.trustStore=./keystore.p12 -Djavax.net.ssl.trustStorePassword=1234 -jar ./payara-micro-5.2022.5.jar --deploy ./navigator-service.war --contextRoot / --port 32412 --sslPort 27274