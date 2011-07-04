# I need to live with two Maven versions, usually the first will be just fine


mvn clean test -DargLine="-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=512m" \
    -Dsurefire.useFile=true \
    -Ph2,test

$MVN surefire-report:report-only
