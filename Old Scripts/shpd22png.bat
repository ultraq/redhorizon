@echo off

echo.
echo Dune 2 SHP file to paletted PNG file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% shp.ShpFileDune2 png.PngFile %1 %2 %3
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   shpd22png (source file) (dest file) (palette file)
echo.
echo eg:
echo   shpd22png d2tank.shp pngtank.png temperat.pal
echo.

:exit
