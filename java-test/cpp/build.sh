#!/usr/bin/env bash

DIR="$( cd "$( dirname "$0"  )" && pwd  )"

cd $DIR
echo "working dir:$DIR"

rm -rf com
rm -rf *.jnilib
javac -d . ../src/main/java/org/rx/test/tools/CPUCounter.java
javah -cp . org.rx.test.tools.CPUCounter

sh make.sh