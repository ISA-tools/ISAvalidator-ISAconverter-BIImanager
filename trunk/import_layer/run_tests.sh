mvn clean test -DargLine="-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=512m" \
    -Dsurefire.useFile=true \
    -Ph2,test \
    -Dtest=SraExportTest
