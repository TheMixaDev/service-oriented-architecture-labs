export _JAVA_OPTIONS="-XX:MaxHeapSize=2G -XX:MaxMetaspaceSize=1G"


java -Djavax.net.ssl.keyStore=./keystore.p12 -Djavax.net.ssl.keyStorePassword=1234 -Djavax.net.ssl.keyStoreType=PKCS12 -Djavax.net.ssl.trustStore=./keystore.p12 -Djavax.net.ssl.trustStorePassword=1234 -jar ./payara-micro-5.2022.5.jar --deploy ./navigator-service.war --contextRoot / --port 32412 --sslPort 27274

haproxy make macos

docker run --name postgres -e POSTGRES_PASSWORD=postgres -d postgres -p 5432:5432

make TARGET=osx USE_OPENSSL=1 \
  SSL_INC=/opt/homebrew/opt/openssl/include \
  SSL_LIB=/opt/homebrew/opt/openssl/lib

1. consul agent -dev -ui -bind=127.0.0.1 -client=0.0.0.0 -http-port=27299 -log-level=INFO
2. ./standalone.sh
2. ./standalone-2.sh
4. sudo ./haproxy -f haproxy.cfg -db