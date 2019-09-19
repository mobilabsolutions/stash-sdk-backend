#!/bin/bash

# global resource variables
export KUBE_NAMESPACE="demo"
export KUBE_APPLY_DATE="`date +%s`"

# ws deployment variables
export KUBE_WS_DEPLOYMENT_CPU_REQUEST="500m"
export KUBE_WS_DEPLOYMENT_CPU_LIMIT="1"
export KUBE_WS_DEPLOYMENT_MEMORY_LIMIT="512Mi"
export KUBE_WS_DEPLOYMENT_MEMORY_REQUEST="300Mi"
export KUBE_WS_DEPLOYMENT_REPLICA_COUNT="1"
export KUBE_WS_DEPLOYMENT_IMAGE_TAG=${TRAVIS_TAG}
export KUBE_WS_DEPLOYMENT_SQL_INSTANCE_NAME="payment-backend-demo"
export KUBE_WS_SENDGRID_PORT="587"
export KUBE_WS_SENDGRID_ADDRESS="stash-merchant-support-noreply@payment.mblb.net"

# notif deployment variables
export KUBE_NOTIF_DEPLOYMENT_CPU_REQUEST="300m"
export KUBE_NOTIF_DEPLOYMENT_CPU_LIMIT="1"
export KUBE_NOTIF_DEPLOYMENT_MEMORY_LIMIT="512Mi"
export KUBE_NOTIF_DEPLOYMENT_MEMORY_REQUEST="300Mi"
export KUBE_NOTIF_DEPLOYMENT_REPLICA_COUNT="1"
export KUBE_NOTIF_DEPLOYMENT_IMAGE_TAG=commit-${TRAVIS_COMMIT}
export KUBE_NOTIF_DEPLOYMENT_SQL_INSTANCE_NAME="payment-backend-demo"

# ws configmap variables
export KUBE_WS_CONFIGMAP_JAVA_OPTS="-Xmx300m -XX:+ExitOnOutOfMemoryError"
export KUBE_WS_CONFIGMAP_SPRING_PROFILE="demo"
export KUBE_WS_CONFIGMAP_DB_PORT="5432"
export KUBE_WS_CONFIGMAP_DB_HOST="localhost"
export KUBE_WS_CONFIGMAP_DB_NAME="payment-sdk-backend-demo"

# notif configmap variables
export KUBE_NOTIF_CONFIGMAP_JAVA_OPTS="-Xmx200m -XX:+ExitOnOutOfMemoryError"
export KUBE_NOTIF_CONFIGMAP_SPRING_PROFILE="demo"
export KUBE_NOTIF_CONFIGMAP_SQL_JDBC_URL="jdbc:postgresql://localhost/payment-sdk-notif-demo"
export KUBE_NOTIF_CONFIGMAP_PAYMENT_WS_URL="https://payment-demo.mblb.net/api/v1/notification"
export KUBE_NOTIF_CONFIGMAP_PSP="ADYEN;BRAINTREE;BS_PAYONE"
export KUBE_NOTIF_CONFIGMAP_SEPARATOR=";"
export KUBE_NOTIF_CONFIGMAP_PARALLELISM="\"5\""
export KUBE_NOTIF_CONFIGMAP_INTERVAL="\"5000\""

# service variables

# ingress variables
export KUBE_INGRESS_STATIC_IP_NAME="payment-backend-demo"
export KUBE_INGRESS_HOST_NAME="payment-demo.mblb.net"
export KUBE_INGRESS_CERT_NAME="payment-demo"
export KUBE_INGRESS_VERSION="v1"