@echo off
set "PATH=%PATH%;C:\Users\1111g\OneDrive\Desktop\Cloud_Based_Identity_verification_app\cloud_based_Identity_verification\backend\tools\apache-maven-3.9.9\bin"
cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.arguments=--app.otp.dev-fallback-enabled=true,--server.error.include-stacktrace=always,--server.error.include-message=always 2>&1
