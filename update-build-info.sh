#!/usr/bin/env bash

app="$1"
path="$app/src/main/java"
package="com.amos.${app/app/flyinn}"
classname="BuildInfo"

# get the latest tag (according to when the tag was created - independent of which commit it refers to)
# WARNING: this can be wrong if we go back to an older commit
tag=$(git for-each-ref --format="%(refname:short)" --count=1 --sort="-creatordate" "refs/tags/*")

# get the date when the current commit was committed
# WARNING: this is totally unrelated to tag
date=$(git log -1 --format="%ci")

builddate=$(date -Iseconds)

ffn="$path/${package//.//}/$classname.java"

cat >"$ffn" << EOF
package $package;

class $classname {
	public static final String GIT_TAG_NAME = "$tag";
	public static final String GIT_COMMIT_DATE = "$date";
	public static final String GIT_BUILD_DATE  = "$builddate";
	public static final String GIT_PACKAGE     = "$package";
}
EOF
