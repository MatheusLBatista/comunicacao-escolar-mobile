#!/bin/bash

# Clean all build artifacts
echo "Cleaning build directories..."
rm -rf build app/build
rm -rf .gradle

echo "Build clean completed!"
echo "Now run: ./gradlew clean build"

