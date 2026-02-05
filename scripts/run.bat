@echo off
cd /d "%~dp0.."
echo Starting Enkelagon...
mvn exec:java -Dexec.mainClass="com.enkelagon.App" -q
