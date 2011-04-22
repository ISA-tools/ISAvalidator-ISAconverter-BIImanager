# Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
# "path:path:..."
# 
#
JDBCPATH=/path/to/jdbc_driver.jar

CP="$JDBCPATH:isatools_deps.jar"

LOAD="load"
ISATAB_DIR="/Users/eamonnmaguire/Downloads/BII-I-1"
CONFIGURATION_DIR="/Users/eamonnmaguire/Downloads/isaconfig-default_v2011-02-18"

java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m\
     -cp "$CP" org.isatools.isatab.manager.SimpleManager $LOAD $ISATAB_DIR $CONFIGURATION_DIR 

