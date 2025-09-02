@echo off

echo.
echo Paletted PNG file to Dune 2 WSA file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile wsa.WsaFileDune2 %1 %2 %3 %4 %5 %6 %7
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   png2wsad2 (source file) (dest file) -w:X -h:Y -n:N -f:F [-loop]
echo.
echo Where:
echo  X = width of each frame
echo  Y = height of each frame
echo  N = number of frames
echo  F = framerate of animation
echo And:
echo  -loop = optional parameter, used to specify that the animation is supposed
echo          to loop.  When using this parameter, there should be 1 more frame
echo          which will act as the special loop frame that connects the last
echo          and first frames.  Note that the loop frame should not be included
echo          when specifying N.
echo.
echo eg:
echo   png2wsad2 anim.png anim.wsa -w:100 -h:50 -n:16 -f:12.5 -loop
echo.

:exit
