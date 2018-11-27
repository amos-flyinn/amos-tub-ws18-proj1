#!/usr/bin/env bash
#Giving permissions to travis for gradle
chmod +x gradlew

#Executing emulator for testing
echo no | android create avd --force -n test -t android-$ANDROID_API_LEVEL_EMULATOR --abi default/armeabi-v7a
emulator -avd test -no-window &
android-wait-for-emulator
adb shell input keyevent 82 &
