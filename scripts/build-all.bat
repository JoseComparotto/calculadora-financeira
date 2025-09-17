@echo off
setlocal enableextensions enabledelayedexpansion
cd /d %~dp0\..
call mvn clean package
