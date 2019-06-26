#!/bin/bash
set -euox pipefail
IFS=$'\n\t'

mvn ${MVN_BUILD_JAVA_OPTS} -q clean package

KEY_FILE=payment-gcp-service-account.json
echo "${KEY_FILE_CONTENT}" | base64 --decode > ${KEY_FILE}

gcloud auth activate-service-account --key-file ${KEY_FILE}

gcloud config set project ${PROJECT_ID}
gcloud auth configure-docker --quiet

WS_IMAGE_NAME="payment-sdk-backend"
WS_BASE_IMAGE=${REGISTRY_HOSTNAME}/${PROJECT_ID}/${WS_IMAGE_NAME}
WS_INITIAL_IMAGE=${WS_BASE_IMAGE}:commit-${TRAVIS_COMMIT}

NOTIF_IMAGE_NAME="payment-sdk-notification"
NOTIF_BASE_IMAGE=${REGISTRY_HOSTNAME}/${PROJECT_ID}/${NOTIF_IMAGE_NAME}
NOTIF_INITIAL_IMAGE=${NOTIF_BASE_IMAGE}:commit-${TRAVIS_COMMIT}

build() {
  echo "Building ${WS_IMAGE_NAME}"
  docker build -t ${WS_IMAGE_NAME} ${TRAVIS_BUILD_DIR}/payment-ws
  echo "Building ${NOTIF_IMAGE_NAME}"
  docker build -t ${NOTIF_IMAGE_NAME} ${TRAVIS_BUILD_DIR}/payment-notifications
}

tag() {
  for TAG in "$@"; do
    echo "Tagging ${WS_BASE_IMAGE}:${TAG}"
    docker tag ${WS_INITIAL_IMAGE} ${WS_BASE_IMAGE}:${TAG}
    echo "Tagging ${NOTIF_BASE_IMAGE}:${TAG}"
    docker tag ${NOTIF_INITIAL_IMAGE} ${NOTIF_BASE_IMAGE}:${TAG}
  done
}

push() {
  echo "Pushing tags for ${WS_BASE_IMAGE}"
  docker push ${WS_BASE_IMAGE}
  echo "Pushing tags for ${NOTIF_BASE_IMAGE}"
  docker push ${NOTIF_BASE_IMAGE}
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
