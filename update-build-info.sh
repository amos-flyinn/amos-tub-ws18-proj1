#!/usr/bin/env bash

mkdir -p "app/src/main/java/com/amos/flyinn/buildinfo/"
cat > "app/src/main/java/com/amos/flyinn/buildinfo/BuildInfo.java" << EOF
package com.amos.flyinn.buildinfo;

public class BuildInfo {
	public static final String GIT_TAG_NAME = "$(git for-each-ref --format="%(refname:short)" --count=1 --sort="-creatordate" "refs/tags/*")";
	public static final String GIT_COMMIT_DATE = "$(git log -1 --format="%ci")";
	public static final String GIT_BUILD_DATE  = "$(date -Iseconds)";
}
EOF
