#!/bin/sh
export MAVEN_OPTS="-Xms256m -Xmx1024m"
#MAVEN_CMD_OPTS="-Pp6spy_oracle_submission,nodrop --offline"
export MAVEN_CMD_OPTS="-Ph2,test"
