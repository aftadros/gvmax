#!/bin/bash
mvn clean site:site site:stage -Dmaven.test.failure.ignore=true
STATUS=$?
if [ $STATUS -eq 0 ]; then
	echo "Site creation successful"
	rm -rf target/site
	mv target/staging target/site
	mvn com.github.github:site-maven-plugin:site -N -e -X
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		echo "Site deployed"
	else
		echo "Site deployment failed"
	fi
else
	echo "Site creation failed"
fi