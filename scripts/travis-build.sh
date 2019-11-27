#!/bin/bash
set -euox pipefail
IFS=$'\n\t'

mvn -q package

PROJECT_ID="mobilabsolutions/stash-sdk-backend"
REGISTRY="docker.pkg.github.com"

docker login ${REGISTRY} -u ${DOCKER_USER} -p ${DOCKER_TOKEN}

WS_IMAGE_NAME="stash-sdk-backend"
WS_BASE_IMAGE=${REGISTRY}/${PROJECT_ID}/${WS_IMAGE_NAME}
WS_INITIAL_IMAGE=${WS_BASE_IMAGE}:commit-${TRAVIS_COMMIT}

NOTIFICATION_IMAGE_NAME="stash-sdk-notification"
NOTIFICATION_BASE_IMAGE=${REGISTRY}/${PROJECT_ID}/${NOTIFICATION_IMAGE_NAME}
NOTIFICATION_INITIAL_IMAGE=${NOTIFICATION_BASE_IMAGE}:commit-${TRAVIS_COMMIT}

build() {
  echo "Building ${WS_INITIAL_IMAGE}"
  docker build -t ${WS_INITIAL_IMAGE} ${TRAVIS_BUILD_DIR}/stash-ws
  echo "Building ${NOTIFICATION_INITIAL_IMAGE}"
  docker build -t ${NOTIFICATION_INITIAL_IMAGE} ${TRAVIS_BUILD_DIR}/stash-notifications
}

tag() {
  for TAG in "$@"; do
    echo "Tagging ${WS_BASE_IMAGE}:${TAG}"
    docker tag ${WS_INITIAL_IMAGE} ${WS_BASE_IMAGE}:${TAG}
    echo "Tagging ${NOTIFICATION_BASE_IMAGE}:${TAG}"
    docker tag ${NOTIFICATION_INITIAL_IMAGE} ${NOTIFICATION_BASE_IMAGE}:${TAG}
  done
}

push() {
  echo "Pushing tags for ${WS_BASE_IMAGE}"
  docker push ${WS_BASE_IMAGE}
  echo "Pushing tags for ${NOTIFICATION_BASE_IMAGE}"
  docker push ${NOTIFICATION_BASE_IMAGE}
}

build

if echo ${TRAVIS_PULL_REQUEST} | egrep '[[:digit:]]+'; then
  tag pr-${TRAVIS_PULL_REQUEST} build-${TRAVIS_BUILD_NUMBER} commit-${TRAVIS_COMMIT}
else
  if [[ ${TRAVIS_BRANCH:-X} == 'master' ]]; then
    tag latest build-${TRAVIS_BUILD_NUMBER} commit-${TRAVIS_COMMIT}
  fi
fi

if [[ ! -z ${TRAVIS_TAG:+X} ]]; then
  tag ${TRAVIS_TAG} build-${TRAVIS_BUILD_NUMBER} commit-${TRAVIS_COMMIT}
fi

push
