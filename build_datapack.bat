@echo off
cd src\main\resources
if exist better-player-locator-bar-1.1.3.zip del better-player-locator-bar-1.1.3.zip
powershell Compress-Archive -Path pack.mcmeta,data -DestinationPath better-player-locator-bar-1.1.3.zip -Force
move better-player-locator-bar-1.1.3.zip ..\..\..\build\
echo Datapack generado en build\better-player-locator-bar-1.1.3.zip