@echo off
setlocal

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Downloading Maven 3.9.9...
mkdir "%MAVEN_HOME%" 2>nul
set TMP_FILE=%TEMP%\maven-download.zip
powershell -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TMP_FILE%'"
powershell -Command "Expand-Archive -Path '%TMP_FILE%' -DestinationPath '%MAVEN_HOME%\..' -Force"
del "%TMP_FILE%" 2>nul
echo Maven 3.9.9 installed.

:runMaven
"%MAVEN_HOME%\bin\mvn.cmd" %*
