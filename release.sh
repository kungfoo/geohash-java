#!/bin/bash

mvn release:clean release:prepare
mvn release:perform