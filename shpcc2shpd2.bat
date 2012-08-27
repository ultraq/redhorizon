@echo off

echo.
echo TD/RA SHP file to Dune 2 SHP file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% shp.ShpFileCNC shp.ShpFileDune2 %1 %2
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   shpcc2shpd2 (source file) (dest file)
echo.
echo eg:
echo   shpcc2shpd2 cnctank.shp d2tank.shp
echo.

:exit
