#!/bin/sh
# really just a modified verion of https://openjfx.io/openjfx-docs/
# Command line example

export PROJECT_DIR="${PROJECT_DIR=unset}"
export PROJECT_NAME=ColorSleuth
export MAIN_CLASS=com.wsdev.colorsleuth.Launcher
export PATH_TO_FX_WIN=$PROJECT_DIR/javafx/javafx-sdk-19-win/lib/
export PATH_TO_FX_MAC=$PROJECT_DIR/javafx/javafx-sdk-19-osx/lib/
export PATH_TO_FX_NIX=$PROJECT_DIR/javafx/javafx-sdk-19-aarch64/lib/
export PATH_TO_FX_A32=$PROJECT_DIR/javafx/javafx-sdk-19-arm32/lib/

if [ "$PROJECT_DIR" = "unset" ] 
then
      echo "ERROR: project dir is not set, please set it in package.sh"
      read -p "Press enter to continue"
      exit 0
else
      echo "Project dir is set.. continuing process."
fi

echo "Compiling main application"

# Compiling main application
javac --module-path $PATH_TO_FX_NIX --module-path $PATH_TO_FX_WIN --module-path $PATH_TO_FX_MAC --add-modules=javafx.controls -d out64 $(find $PROJECT_DIR/src/com/wsdev/colorsleuth -name "*.java")
javac --module-path $PATH_TO_FX_A32 --module-path $PATH_TO_FX_WIN --module-path $PATH_TO_FX_MAC --add-modules=javafx.controls -d out32 $(find $PROJECT_DIR/src/com/wsdev/colorsleuth -name "*.java")

find $PATH_TO_FX_NIX/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out64 \;
find $PATH_TO_FX_WIN/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out64 \;
find $PATH_TO_FX_MAC/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out64 \;
find $PATH_TO_FX_MAC/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out32 \;
find $PATH_TO_FX_WIN/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out32 \;
find $PATH_TO_FX_A32/{javafx.base.jar,javafx.graphics.jar,javafx.controls.jar} -exec unzip -nq {} -d out32 \;


echo "Copying system files to x64 artifact"

# feel free to comment out ones you don't want (that are not required by your os)
cp $PATH_TO_FX_NIX/{libprism*.so,libjavafx*.so,libglass*.so,libdecora_sse.so} out64
cp $PATH_TO_FX_WIN/../bin/{prism*.dll,javafx*.dll,glass.dll,decora_sse.dll} out64
cp $PATH_TO_FX_MAC/{libprism*.dylib,libjavafx*.dylib,libglass.dylib,libdecora_sse.dylib} out64

echo "Copying system files to ARM32 support artifact"

cp $PATH_TO_FX_A32/{libprism*.so,libjavafx*.so,libglass*.so,libdecora_sse.so} out32
cp $PATH_TO_FX_WIN/../bin/{prism*.dll,javafx*.dll,glass.dll,decora_sse.dll} out32
cp $PATH_TO_FX_MAC/{libprism*.dylib,libjavafx*.dylib,libglass.dylib,libdecora_sse.dylib} out32


mkdir build

echo "Creating x64 artifact"
jar --create --file=build/$PROJECT_NAME-all64.jar --main-class=$MAIN_CLASS -C out64 .

echo "Creating ARM32 support artifact"
jar --create --file=build/$PROJECT_NAME+arm32-no_linux_64_support.jar --main-class=$MAIN_CLASS -C out32 .

read -p "Press Enter to continue"