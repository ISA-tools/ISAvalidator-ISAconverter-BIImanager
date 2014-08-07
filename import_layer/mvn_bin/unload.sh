#!/bin/sh
SOURCEPATH=$1
if [ "$SOURCEPATH" == "" ]; then 
  cat <<EOT
  

  Unloads from the BII database an ISA-TAB archive which was previously submitted. Usage:

    $(basename $0) <study-accession> [<dest-path>]
    $(basename $0) -ts <submission-timestamp> [<dest-path>]
  
  <study-accession> is the accession of he study to be unloaded.  

  <submission-timestamp> is the submission timestamp, which si provided by the submission command or may be retrieved
  from the BII database. It can be an integer representation of a time instant, or in the form: yyyy-mm-dd hh:mm:ss.fffffffff
  
  <dest-path> is the directory where to save persistence execution report and log. 
  Default is current working directory. 


  Related Files

		config/: a set of configuration files are stored here.
		config.sh: defines common configuration options for the Submission command line tools

PLEASE NOTE: the unloading feature has been designed mainly having in mind the use case where a submission with 
problems is unloaded from the database just after it has been uploaded. In other cases, entities which have
been reused with later submissions won't be removed. For instance, if an ontology term is reused in a study different
than the one being removed, that term is kept in the database.

EOT
exit 1
fi

if [ "$1" == "-ts" ]; then
  java_args="-ts $2"
  shift
else 
  java_args="$1"
fi

BASEPATH=$(pwd)
. $BASEPATH/config.sh

DESTPATH=$2
if [ "$DESTPATH" != "" ]; then
  java_args="$java_args $DESTPATH"
fi


cd ..
mvn \
	  $MAVEN_CMD_OPTS \
	  -Dbioinvindex.config-path="$BASEPATH/../../config" \
	  -Dexec.mainClass="org.isatools.isatab.commandline.UnloadShellCommand" \
	  -Dexec.args="$java_args" \
  exec:java \

echo Finished.
