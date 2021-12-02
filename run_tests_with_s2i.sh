#!/usr/bin/env bash

# launch the tests without deploying the application
./mvnw -s .github/mvn-settings.xml verify -pl tests -Popenshift-it
