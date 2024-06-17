#!/usr/bin/env bash

PARENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -pl :IF-parent -q -DforceStdout)
BASE_VERSION=$(mvn help:evaluate -Dexpression=project.version -pl :IF -q -DforceStdout)

if [[ $PARENT_VERSION != "$BASE_VERSION" ]]; then
  echo "IF-parent and IF versions mismatch"
  exit 1
fi

if [[ $PARENT_VERSION != *-SNAPSHOT ]]; then
  exit 0
fi

if ! mvn clean install -B; then
  echo "Unable to build IF"
  exit 1
fi

if ! mvn deploy -N -pl :IF-parent -P deploy -s ./.github/deployment/settings.xml -B -Dgpg.passphrase="$1" -Ddeploy.username="$2" -Ddeploy.password="$3"; then
  echo "Unable to deploy IF-parent"
  exit 1
fi

if ! mvn deploy -pl :IF -P deploy -s ./.github/deployment/settings.xml -B -Dgpg.passphrase="$1" -Ddeploy.username="$2" -Ddeploy.password="$3"; then
  echo "Unable to deploy IF"
fi
