#!/bin/bash

# Copyright (C) 2018 Genymobile
# Under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Wrapper script to invoke gradle from meson
set -e

if [[ "$EUID" == 0 ]]
then
    echo "(not invoking gradle, since we are root)" >&2
    exit 1
fi

PROJECT_ROOT="$1"
OUTPUT="$2"
BUILDTYPE="$3"

# gradlew is in the parent of the server directory
GRADLE=${GRADLE:-$PROJECT_ROOT/../gradlew}

if [[ "$BUILDTYPE" == debug ]]
then
    "$GRADLE" -p "$PROJECT_ROOT" assembleDebug
    cp "$PROJECT_ROOT/build/outputs/apk/debug/fakeinputlib-debug.apk" "$OUTPUT"
else
    "$GRADLE" -p "$PROJECT_ROOT" assembleRelease
    cp "$PROJECT_ROOT/build/outputs/apk/release/fakeinputlib-release-unsigned.apk" "$OUTPUT"
fi
