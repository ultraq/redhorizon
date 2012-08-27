@echo off

echo.
echo Paletted PNG file to Dune 2 SHP file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile shp.ShpFileDune2 %1 %2 %3 %4 %5 %6
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   png2shpd2 (source file) (dest file) -w:X -h:Y -n:N [-faction]
echo.
echo Where:
echo  X = width of each image
echo  Y = height of each image
echo  N = number of images
echo And:
echo  -faction = optional parameter, tells the program to generate a Dune 2 SHP
echo             file whose red-palette colours (indexes 144-150) will be
echo             exchanged for the proper faction colours in-game. 
echo.
echo eg:
echo   png2shpd2 pngtank.png d2tank.shp -w:48 -h:48 -n:32 -faction
echo.

:exit
