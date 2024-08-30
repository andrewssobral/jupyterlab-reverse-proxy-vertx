#!/bin/bash

# mvn exec:java -Dexec.mainClass="com.example.App"
mvn -X exec:java -Dexec.mainClass="com.example.App"
# java -cp target/JupyterLabProxy-1.0-SNAPSHOT.jar com.example.App