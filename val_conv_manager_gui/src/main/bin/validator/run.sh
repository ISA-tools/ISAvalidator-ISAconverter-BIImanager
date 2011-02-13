#!/bin/sh
java -Xms256m -Xmx1024m -XX:PermSize=64m -XX:MaxPermSize=128m\
     -cp isatools_deps.jar org.isatools.gui.validator.ValidatorInvoker
