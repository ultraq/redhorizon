@echo off

echo.
echo Paletted PNG file to TD/RA SHP file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile shp.ShpFileCNC %1 %2 %3 %4 %5
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   png2shpcc (source file) (dest file) -w:X -h:Y -n:N
echo.
echo Where:
echo  X = width of each image
echo  Y = height of each image
echo  N = number of images
echo.
echo eg:
echo   png2shpcc pngtank.png cctank.shp -w:48 -h:48 -n:32
echo.

:exit
