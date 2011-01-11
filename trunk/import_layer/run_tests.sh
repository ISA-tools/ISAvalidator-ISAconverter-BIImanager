mvn -DargLine="-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m" \
    -Dsurefire.useFile=true \
    -Ph2,test \
    SraExportTest
    
mvn surefire-report:report-only