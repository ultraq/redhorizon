@echo off

echo.
echo Multiple paletted PNG files to Dune 2 SHP file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile(MULTI) shp.ShpFileDune2 %1 %2 %3
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   mpng2shpd2 (source file pattern) (dest file) [-faction]
echo.
echo Where:
echo   pattern = name of the file without numbering information.  If the names
echo             of the files are: pngtank000.png, pngtank001.png, etc, then the
echo             pattern is pngtank.png.  Files must be numbered from 000 to the
echo             number of source files you have, up to a max of 999.
echo And:
echo  -faction = optional parameter, tells the program to generate a Dune 2 SHP
echo             file whose red-palette colours (indexes 144-150) will be
echo             exchanged for the proper faction colours in-game. 
echo.
echo eg:
echo   mpng2shpd2 pngtank.png d2tank.shp
echo.

:exit
