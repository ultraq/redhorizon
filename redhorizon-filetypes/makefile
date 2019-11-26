#!/bin/bash
# Compiles the C++ part which I haven't converted to Java yet

libMixFileKey.dylib: source/MixFileKey.cpp source/MixFileKey.h source/MixFileKeyJNI.h
	g++ -fPIC -shared -o libMixFileKey.dylib \
	-I $(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	source/MixFileKey.cpp -lc
