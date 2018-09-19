#!/usr/sh

mkdir JavaEncFS.app
mkdir JavaEncFS.app/Contents
mkdir JavaEncFS.app/Contents/MacOS
mkdir JavaEncFS.app/Contents/Ressources
mkdir JavaEncFS.app/Contents/Ressources/Java
cp info.plist JavaEncFS.app/Contents
cp /System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/JavaApplicationStub JavaEncFS.app/Contents/MacOS
cp src/main/image/JavaEncFS.icns JavaEncFS.app/Contents/Ressources
echo APPL???? >> JavaEncFS.app/Contents/PkgInfo
cp target/encFS-1.0-jar-with-dependencies.jar JavaEncFS.app/Contents/Ressources/Java/JavaEncFS.jar
/Developer/Tools/SetFile -a B JavaEncFS.app.app
