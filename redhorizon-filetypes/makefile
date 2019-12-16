#!/bin/bash
# Compiles the C++ part which I haven't converted to Java yet

linux: source/MixFileKey.cpp source/MixFileKey.h source/MixFileKeyJNI.h
	g++ -shared -o libMixFileKey.so \
	-I $(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/linux \
	source/MixFileKey.cpp -lc

macos: source/MixFileKey.cpp source/MixFileKey.h source/MixFileKeyJNI.h
	g++ -shared -o libMixFileKey.dylib \
	-I $(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	source/MixFileKey.cpp -lc

windows: source\MixFileKey.cpp source\MixFileKey.h source\MixFileKeyJNI.h
	cl /I "%JAVA_HOME%\include" /I "%JAVA_HOME%\include\win32" /LD source\MixFileKey.cpp
	del MixFileKey.exp
	del MixFileKey.lib
	del MixFileKey.obj
