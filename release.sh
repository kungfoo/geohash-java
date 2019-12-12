#!/bin/bash
mvn clean deploy
mvn nexus-staging:release
