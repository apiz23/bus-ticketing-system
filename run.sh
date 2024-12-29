#!/bin/bash

# Compile the Java files using a find command to list files
javac -d out -cp ".:postgresql-42.7.4.jar" $(find src -name "*.java")

# Run the compiled Java program
java -cp ".:out:postgresql-42.7.4.jar:core-3.3.0.jar:javase-3.3.0.jar" Main
