#!/bin/bash
# Compiles the C++ part which I haven't converted to Java yet

libMixFileKey.so: source/MixFileKey.cpp source/MixFileKey.h source/MixFileKeyJNI.h
	g++ -fPIC -shared -o libMixFileKey.so \
	-I $(JAVA_HOME)/include \
	-I $(JAVA_HOME)/include/darwin \
	-I $(JAVA_HOME)/include/linux \
	source/MixFileKey.cpp -lc
