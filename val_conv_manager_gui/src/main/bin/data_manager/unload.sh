# Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
# "path:path:..."
# 
#
JDBCPATH=/path/to/jdbc_driver.jar

CP="$JDBCPATH:isatools_deps.jar"

UNLOAD="unload"



java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m\
     -cp "$CP" org.isatools.isatab.manager.SimpleManager $UNLOAD "BII-S-1" "BII-S-2" 
