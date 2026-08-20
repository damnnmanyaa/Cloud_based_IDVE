@echo off
set MAVEN_OPTS=-Dlogging.level.com.idve.backend=DEBUG -Dspring-boot.run.arguments=--app.otp.dev-fallback-enabled=true
C:\Users\1111g\OneDrive\Desktop\Cloud_Based_Identity_verification_app\cloud_based_Identity_verification\backend\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run -Dspring-boot.run.arguments=--app.otp.dev-fallback-enabled=true 2>&1
