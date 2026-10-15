@echo off
echo Starting ReBuild Exchange on http://localhost:8080...
cd /d "%~dp0rebuild-exchange-backend"
java -jar target\rebuild-exchange-backend-1.0.0-SNAPSHOT.jar
pause
