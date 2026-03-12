#!/bin/bash
cd "$(dirname "$0")/.."
echo "Building Enkelagon..."
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
    exit 1
fi
