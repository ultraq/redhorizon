#!/bin/bash
# Compiles the C++ part which I haven't converted to Java yet

libMixFileKey.so: C++/MixFileKey.cpp C++/MixFileKey.h C++/MixFileKeyJNI.h
	g++ -fPIC -shared -o libMixFileKey.so \
	-I $JAVA_HOME/include \
	-I $JAVA_HOME/include/linux \
	C++/MixFileKey.cpp -lc
