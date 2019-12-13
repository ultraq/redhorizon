@echo off

echo.
echo PCX file to CPS file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% pcx.PcxFile cps.CpsFile %1 %2 %3
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   pcx2cps (source file) (dest file) [-nopal]
echo.
echo Where:
echo  The source file is a 320x200 PCX image.  If creating a paletted CPS, then
echo  the PCX file must have an internal palette.
echo And:
echo  -nopal = optional parameter, used if you want to create an unpaletted CPS
echo           file.
echo.
echo eg:
echo   pcx2cps image.pcx image.cps -nopal
echo.

:exit
