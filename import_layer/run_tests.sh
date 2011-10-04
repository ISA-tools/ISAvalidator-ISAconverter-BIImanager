# I need to live with two Maven versions, usually leaving MVN unset will be just fine
if [ "$MVN" == "" ]; then
  echo "Setting 'mvn' as default Maven command, you possibly need to setup Maven 2 via export MVN=<path> before invoking me"
  MVN=mvn
fi
#MVN="mvn2 -s $HOME/.m2_old/settings.xml -Dmaven.repo.local=$HOME/.m2_old/repository"

$MVN clean test -DargLine="-Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=512m" \
    -Dsurefire.useFile=true \
    -Ph2,test

$MVN surefire-report:report-only
