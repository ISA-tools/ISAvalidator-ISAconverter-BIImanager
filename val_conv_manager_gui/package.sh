#!/bin/sh
# Use this file to build all the packages about ISA-Tools
# More details in the POM. 
#

#MVNOPTS="--offline"

pwd=$(dirname $0)
cd $pwd

cd ../graph2tab
mvn $MVNOPTS -Dmaven.test.skip=true clean install

cd ../import_layer
mvn $MVNOPTS -Ptools -Dmaven.test.skip=true clean install

cd ../val_conv_manager_gui
mvn $MVNOPTS -Pbuild_base,tools -Dmaven.test.skip=true clean package 

cd target

# This is necessary due to some wrong packaging in one of our dependencies
# Final jar won't work unless you remove this silly file
zip -d isatools_deps.jar '\*.class'

cd .. 

mvn $MVNOPTS -Pbuild_validator,tools -Dmaven.test.skip=true package
mvn $MVNOPTS -Pbuild_data_mgr,tools -Dmaven.test.skip=true package
mvn $MVNOPTS -Pbuild_converter,tools -Dmaven.test.skip=true package
