mvn2 clean test -DargLine="-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=512m" \
    -Dsurefire.useFile=true \
    -Ph2,test 
#-Dtest=ISAConfigurationBatchTest
mvn -Ph2,test surefire-report:report-only