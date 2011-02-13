SOURCEPATH=$1
if [ "$SOURCEPATH" == "" ]; then 
  cat <<EOT
  

  Imports from an ISA-TAB archive into the BII database. Usage:

    $(basename $0) <source-path> [<dest-path>]
  
  <source-path> is the path of the ISATAB submission. It can be a directory or a path to the investigation file. 
  If it is a directory, I will searh i_xxxx.txt or investigation.csv files in it. All other submission files will 
  be loaded from the source path, using the references in the investigation file. 

  <dest-path> is the directory where to save persistence execution report and log. 
  Default is '<source-path>'. 


  Related Files

		config/: a set of configuration files are stored here.
		config.sh: defines common configuration options for the Submission command line tools

EOT
exit 1
fi
  
BASEPATH=$(pwd)
. $BASEPATH/config.sh

DESTPATH=$2

java_args="$SOURCEPATH"
if [ "$DESTPATH" != "" ]; then
  java_args="$java_args $DESTPATH"
fi


cd ..
mvn \
  $MAVEN_CMD_OPTS \
  -Dbioinvindex.config-path="$BASEPATH/../../config" \
  -Dexec.mainClass="org.isatools.isatab.commandline.PersistenceShellCommand" \
  -Dexec.args="$java_args" \
  exec:java

echo Finished.
