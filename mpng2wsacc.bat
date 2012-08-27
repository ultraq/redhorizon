@echo off

echo.
echo Multiple paletted PNG files to TD/RA WSA file converter
echo By Emanuel Rabina
echo.

set RH_CLASSPATH=*;Libraries\*
set RH_CONVERTER=redhorizon.utilities.converter.ConverterCommandLine

java -classpath %RH_CLASSPATH% %RH_CONVERTER% png.PngFile(MULTI) wsa.WsaFileCNC %1 %2 %3 %4 %5
if errorlevel 1 goto noparam

goto exit

:noparam
echo Usage:
echo   png2wsacc (source file pattern) (dest file) -f:F [-loop] [-nohires]
echo.
echo Where:
echo  F = framerate of animation
echo  pattern = name of the file without numbering information.  If the names of
echo            the files are: pngtank000.png, pngtank001.png, etc, then the
echo            pattern is pngtank.png.  Files must be numbered from 000 to the
echo            number of source files you have, up to a max of 999.
echo And:
echo  -loop = optional parameter, used to specify that the animation is supposed
echo          to loop.  When using this parameter, the last file in the source
echo          file pattern will act as the special loop frame that connects the
echo          last and first frames.
echo  -nohires = optional parameter, tells the program to not generate the PAL
echo             file used by TD/RA when playing WSAs at a higher resolution.
echo             By default a PAL file is created with the same name as the WSA
echo             but with a .pal extension.
echo.
echo eg:
echo   png2wsacc anim.png anim.wsa -w:100 -h:50 -n:16 -f:12.5 -loop
echo.

:exit
