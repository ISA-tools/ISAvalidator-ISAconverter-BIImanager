#!/bin/sh
# 


# Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
# "path:path:..."
# 
#
JDBCPATH=/path/to/jdbc_driver.jar

java -jar isatools_deps.jar -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m -cp "$JDBCPATH"
