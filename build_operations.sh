#!/usr/bin/env bash

set -euo pipefail

./gradlew assembleMobileDebug \
          connectedMobileDebugAndroidTest


