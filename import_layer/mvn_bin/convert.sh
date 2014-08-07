#!/bin/sh
CONV_TARGET=$1
SOURCEPATH=$2

if [ "$CONV_TARGET" == "" -o "$SOURCEPATH" == "" ]; then 
  cat <<EOT
  
   Converts an ISATAB submission into a different format. Usage:   

    $(basename $0) magetab|prideml|sra|all <source-path> [<dest-path>]
  
  <source-path> is the path of the ISATAB submission. It can be a directory or a path to the investigation file. 
  If it is a directory, I will searh i_xxxx.txt or investigation.csv files in it. All other submission files will 
  be loaded from the source path, using the references in the investigation file. 
  
  <dest-path> is the path where to put the results. 

  Related Files and properties

		config/: a set of configuration files are stored here.
		config.sh: defines common configuration options for the Submission command line tools

EOT
exit 1
fi
    
BASEPATH=$(pwd)
. $BASEPATH/config.sh

DESTPATH=$3
java_args="$SOURCEPATH"
if [ "$DESTPATH" != "" ]; then
  java_args="$java_args $DESTPATH"
fi


if [ "$CONV_TARGET" == "all" ]; then
  targets="magetab prideml sra"
else
  targets="$CONV_TARGET"
fi

cd ..
for target in $targets
do 

	if [ "$target" == "magetab" ]; then
	  class=MAGETABDispatchShellCommand
	elif [ "$target" == "prideml" ]; then
	  class=PrideDispatchShellCommand
	elif [ "$target" == "sra" ]; then
    class=SraDispatchShellCommand
  else
    echo ""
    echo ""
    echo "ERROR: The Conversion Target $target is not valid"
    echo ""
    exit 2
  fi
   
  mvn \
	  $MAVEN_CMD_OPTS \
    -Dbioinvindex.config-path="$BASEPATH/../../config" \
    -Dexec.mainClass="org.isatools.isatab.commandline.$class" \
    -Dexec.args="$java_args" \
    exec:java

  echo Conversion to $target finished.
done

echo Command Finished.
