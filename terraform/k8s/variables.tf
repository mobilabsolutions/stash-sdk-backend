variable "cluster_endpoint" {
    type = string
    description = "Endpoint of the GKE cluster"
}

variable "cluster_ca_certificate" {
    type = string
    description = "Certificate of the GKE cluster"
}

variable "namespace" {
    type = string
    description = "Name of the k8s namespace"
}

variable "secret_name" {
    type = string
    description = "Name of the k8s secret"
}

variable "secret_db_user_name" {
    type = string
    description = "User name of the DB user"
}

variable "secret_db_password" {
    type = string
    description = "Password of the DB user"
}

variable "secret_jwt_secret" {
    type = string
    description = "Secret for signing JWT tokens"
}

variable "secret_notification_user_name" {
    type = string
    description = "User name for incoming payment notifications"
}

variable "secret_notification_password" {
    type = string
    description = "Password for incoming payment notifications"
}

variable "secret_stash_sdk_secret_key" {
    type = string
    description = "Secret key for Stash Payment SDK"
}

variable "secret_backend_name" {
    type = string
    description = "Name of the k8s secret for backend"
}

variable "secret_backend_account_path" {
    type = string
    description = "Path of the backend service account"
}