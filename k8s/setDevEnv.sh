#!/bin/bash

# global resource variables
export KUBE_NAMESPACE="dev"
export KUBE_APPLY_DATE="`date +%s`"

# ws deployment variables
export KUBE_WS_DEPLOYMENT_CPU_REQUEST="400m"
export KUBE_WS_DEPLOYMENT_CPU_LIMIT="1"
export KUBE_WS_DEPLOYMENT_MEMORY_LIMIT="512Mi"
export KUBE_WS_DEPLOYMENT_MEMORY_REQUEST="300Mi"
export KUBE_WS_DEPLOYMENT_REPLICA_COUNT="1"
export KUBE_WS_DEPLOYMENT_IMAGE_TAG=commit-${TRAVIS_COMMIT}
export KUBE_WS_DEPLOYMENT_SQL_INSTANCE_NAME="payment-backend-dev"

# notif deployment variables
export KUBE_NOTIF_DEPLOYMENT_CPU_REQUEST="300m"
export KUBE_NOTIF_DEPLOYMENT_CPU_LIMIT="1"
export KUBE_NOTIF_DEPLOYMENT_MEMORY_LIMIT="512Mi"
export KUBE_NOTIF_DEPLOYMENT_MEMORY_REQUEST="300Mi"
export KUBE_NOTIF_DEPLOYMENT_REPLICA_COUNT="1"
export KUBE_NOTIF_DEPLOYMENT_IMAGE_TAG=commit-${TRAVIS_COMMIT}
export KUBE_NOTIF_DEPLOYMENT_SQL_INSTANCE_NAME="payment-backend-dev"

# ws configmap variables
export KUBE_WS_CONFIGMAP_JAVA_OPTS="-Xmx300m -XX:+ExitOnOutOfMemoryError"
export KUBE_WS_CONFIGMAP_SPRING_PROFILE="dev"
export KUBE_WS_CONFIGMAP_SQL_JDBC_URL="jdbc:postgresql://localhost/payment-sdk-backend-dev"
export KUBE_WS_CONFIGMAP_KAFKA_HOST="kafka.dev-kafka:9092"
export KUBE_WS_CONFIGMAP_KAFKA_TOPIC_NAME="transactions"

# notif configmap variables
export KUBE_NOTIF_CONFIGMAP_JAVA_OPTS="-Xmx200m -XX:+ExitOnOutOfMemoryError"
export KUBE_NOTIF_CONFIGMAP_SPRING_PROFILE="dev"
export KUBE_NOTIF_CONFIGMAP_SQL_JDBC_URL="jdbc:postgresql://localhost/payment-sdk-notif-dev"
export KUBE_NOTIF_CONFIGMAP_PAYMENT_WS_URL="https://payment-dev.mblb.net/v1/api/notification"
export KUBE_NOTIF_CONFIGMAP_PSP="ADYEN;BRAINTREE;BS_PAYONE"
export KUBE_NOTIF_CONFIGMAP_SEPARATOR=";"
export KUBE_NOTIF_CONFIGMAP_PARALLELISM="\"5\""
export KUBE_NOTIF_CONFIGMAP_INTERVAL="\"5000\""

# service variables

# ingress variables
export KUBE_INGRESS_STATIC_IP_NAME="payment-backend-dev"
export KUBE_INGRESS_HOST_NAME="payment-dev.mblb.net/v1"
export KUBE_INGRESS_CERT_NAME="payment-dev"
