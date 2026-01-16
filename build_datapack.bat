@echo off
cd src\main\resources
if exist ${mod_file_name}-${mod_version}.zip del ${mod_file_name}-${mod_version}.zip
powershell Compress-Archive -Path pack.mcmeta,data -DestinationPath ${mod_file_name}-${mod_version}.zip -Force
move ${mod_file_name}-${mod_version}.zip ..\..\..\build\
echo Datapack generado en build\${mod_file_name}-${mod_version}.zip