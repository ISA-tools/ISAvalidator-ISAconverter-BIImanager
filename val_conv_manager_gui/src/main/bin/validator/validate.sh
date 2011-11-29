#!/bin/sh
#


# Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
# "path:path:..."
#
#
CP="isatools_deps.jar"

VALIDATE="validate"
ISATAB_DIR="mydir/BII-I-1"
CONFIGURATION_DIR="config/default-config"

java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m -cp "$CP" org.isatools.isatab.manager.SimpleManager $VALIDATE $ISATAB_DIR $CONFIGURATION_DIR