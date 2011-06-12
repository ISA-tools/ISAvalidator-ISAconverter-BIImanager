# Change this with your driver. It is added to the Java's class path, so, in case of multiple files, use the usual syntax
# "path:path:..."
# 
#
JDBCPATH=/path/to/jdbc_driver.jar

CP="$JDBCPATH:isatools_deps.jar"

# to reindex everything, just use the REINDEXALL variable instead. There is obviously no need to specify the studies to reindex.
REINDEXALL="reindexAll"

# to reindex selected studies, use the REINDEX variable and add the Studies you wish to reindex, e.g. "BII-S-1" "BII-S-8"
REINDEX="reindex"




java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m\
     -cp "$CP" org.isatools.isatab.manager.SimpleManager $REINDEX "BII-S-8"

