#!/bin/bash
set -euox pipefail
IFS=$'\n\t'

source ./k8s/setDevEnv.sh

gcloud container clusters get-credentials ${CLUSTER_NAME} --region ${REGION} --project ${PROJECT_ID}

envsubst < k8s/resources/ws-deployment.yaml | kubectl apply -f -
envsubst < k8s/resources/ws-configmap.yaml | kubectl apply -f -
envsubst < k8s/resources/ws-service.yaml | kubectl apply -f -

envsubst < k8s/resources/notif-deployment.yaml | kubectl apply -f -
envsubst < k8s/resources/notif-configmap.yaml | kubectl apply -f -
envsubst < k8s/resources/notif-service.yaml | kubectl apply -f -

envsubst < k8s/resources/ingress.yaml | kubectl apply -f -
