#! /usr/bin/env bash
set -o nounset
set -o errexit

pushd ./johnson-runtime
	mvn install
popd
pushd ./johnson-codegen
	mvn install
popd
pushd ./johnson-codegen-maven-plugin
	mvn install
popd
pushd ./johnson-codegen-samples
	mvn install
popd
