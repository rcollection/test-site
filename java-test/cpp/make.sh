#!/usr/bin/env bash
HEADER=/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/

g++ -shared tools.cpp -I${HEADER} -o libtools.jnilib
#g++ -shared algorithm.cpp -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -o libalgorithm.jnilib
