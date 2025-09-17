@echo off
setlocal enableextensions enabledelayedexpansion
cd /d %~dp0\..
call mvn -q -DskipITs -pl calculadora-financeira-cli -am package || goto :eof
java -jar calculadora-financeira-cli\target\calculadora-financeira-cli-*-shaded.jar
