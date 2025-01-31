#!/bin/bash

# Get the directory containing JAR files from the first argument passed to the script
JAR_DIR="$1"

# Check if JAR_DIR is provided and is a valid directory
if [[ -z "$JAR_DIR" || ! -d "$JAR_DIR" ]]; then
    echo "Error: Directory '$JAR_DIR' does not exist or is not specified."
    exit 1
fi

# Install each JAR with a fixed group ID
for jar in "$JAR_DIR"/*.jar; do
  # Check if there are any JAR files in the directory
      if [[ ! -e "$jar" ]]; then
          echo "No JAR files found in '$JAR_DIR'."
          break
      fi

    # Extract artifact name (filename without .jar)
    artifact=$(basename "$jar" .jar)

    # Print the artifact name
    echo "Installing artifact: $artifact"

    mvn install:install-file \
        -Dfile="$jar" \
        -DgroupId=com.example.libs \
        -DartifactId="$artifact" \
        -Dversion=1.0 \
        -Dpackaging=jar
done