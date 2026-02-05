#!/bin/bash
cd "$(dirname "$0")/.."
echo "Starting Enkelagon..."
mvn exec:java -Dexec.mainClass="com.enkelagon.App" -q
