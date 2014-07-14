#!/bin/bash
mvn clean test -Dmaven.test.failure.ignore=true -Pgvmax-sonar
STATUS=$?
if [ $STATUS -eq 0 ]; then
	mvn sonar:sonar -Pgvmax-sonar
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		echo "Sonar deployed"
	else
		echo "Sonar deployment failed"
	fi
else
	echo "Sonar failed"
fi