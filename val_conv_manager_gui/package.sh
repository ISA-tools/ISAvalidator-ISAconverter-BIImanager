#!/bin/sh
# Use this file to build all the packages about ISA-Tools
# More details in the POM. 
#

#MVNOPTS="--offline"

function pause(){
   read -p "$*"
}

PACKAGE_TYPE=$1
echo "ISAvalidator-ISAconverter-BIImanager packaging for type " $PACKAGE_TYPE

if [ "$PACKAGE_TYPE" = "mixs"  ]
then
    CONFIGURATION=isaconfig-mixs-v4.zip
else
    CONFIGURATION=isaconfig-default_v2014-01-16.zip
fi

echo "Configuration file: " $CONFIGURATION

#pause "check configuratio filename and press enter"

if hash curl 2>/dev/null; then
   echo "curl is installed, will download configurations next"
else
   echo "curl is not installed, install it and then run package.sh again"
   exit 1
fi

curl -L -O http://bitbucket.org/eamonnmag/isatools-downloads/downloads/"$CONFIGURATION"

mv $CONFIGURATION ../config

WD=$(pwd)

pwd
cd ../config
unzip $CONFIGURATION

filename="${CONFIGURATION%.*}"

echo $filename

#pause "Check filename"

mv $filename default-config

#pause "Renamed file"

rm -f $CONFIGURATION
pwd
cd $WD
echo "Changing back to target..."
pwd


#pwd=$(dirname $0)
#cd $pwd

cd ../import_layer
mvn $MVNOPTS -Ptools -Dmaven.test.skip=true clean install

cd ../val_conv_manager_gui
mvn $MVNOPTS -Pbuild_base,tools -Dmaven.test.skip=true clean package

cd target

# This is necessary due to some wrong packaging in one of our dependencies
# Final jar won't work unless you remove this silly file
zip -d isatools_deps.jar '\*.class'

cd .. 

pwd

cd target
cp -r ../src/main/assembly/manifests/validator/META-INF .
zip -r isatools_deps.jar META-INF/
cd ..
mvn $MVNOPTS -Pbuild_validator,tools -Dmaven.test.skip=true package

cd target
cp -r ../src/main/assembly/manifests/data_manager/META-INF .
zip -r isatools_deps.jar META-INF/
cd ..
mvn $MVNOPTS -Pbuild_data_mgr,tools -Dmaven.test.skip=true package

cd target
cp -r ../src/main/assembly/manifests/converter/META-INF .
zip -r isatools_deps.jar META-INF/
cd ..
zip -r target/isatools_deps.jar src/main/assembly/manifests/converter/META-INF/
mvn $MVNOPTS -Pbuild_converter,tools -Dmaven.test.skip=true package
