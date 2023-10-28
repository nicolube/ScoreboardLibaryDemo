#!/bin/bash


current_dir=$(pwd)

# Create server dir
mkdir -p server

# Compile Demo plugin
chmod +x DemoPlugin/gradlew
cd DemoPlugin/
./gradlew clean build
cd $current_dir

# Download PurpurMC 1.20.1 (2062)
wget https://api.purpurmc.org/v2/purpur/1.20.1/2062/download -O server/server.jar

# Copy Plugin to server
mkdir -p server/plugins
cp DemoPlugin/build/libs/*SNAPSHOT.jar server/plugins/
