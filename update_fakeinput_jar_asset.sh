#!/usr/bin/env bash

set -euo pipefail

./gradlew :fakeinputlib:build 

if [[ ! -f ./x ]]; then
    meson fakeinput_dist --buildtype release --strip -Db_lto=true
fi

(
    cd fakeinput_dist; \
    ninja;
)

cp fakeinput_dist/fakeinputlib/flyinn-fakeinputlib.jar app/src/main/res/raw/flyinn_fakeinputlib.jar
