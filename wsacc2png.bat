@echo off

echo.
echo TD/RA WSA file to paletted PNG file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% wsa.WsaFileCNC png.PngFile %1 %2
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   wsacc2png (source file) (dest file)
echo.
echo eg:
echo   wsacc2png ccanim.wsa pnganim.png
echo.

:exit
