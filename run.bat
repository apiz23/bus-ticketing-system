@echo off
rem Compile the Java files using PowerShell to list files
powershell -Command "javac -d out -cp '.;postgresql-42.7.4.jar;core-3.3.0.jar;javase-3.3.0.jar' (Get-ChildItem -Recurse -Path src -Filter '*.java').FullName"

rem Run the compiled Java program
java -cp ".;out;postgresql-42.7.4.jar;core-3.3.0.jar;javase-3.3.0.jar" Main
