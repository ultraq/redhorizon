@echo off

echo.
echo Paletted PNG file to TD/RA WSA file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile wsa.WsaFileCNC %1 %2 %3 %4 %5 %6 %7 %8
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   png2wsacc (source file) (dest file) -w:X -h:Y -n:N -f:F [-loop] [-nohires]
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
echo  -nohires = optional parameter, tells the program to not generate the PAL
echo             file used by TD/RA when playing WSAs at a higher resolution.
echo             By default a PAL file is created with the same name as the WSA
echo             but with a .pal extension.
echo.
echo eg:
echo   png2wsacc anim.png anim.wsa -w:100 -h:50 -n:16 -f:12.5 -loop
echo.

:exit
