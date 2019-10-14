variable "project" {
  default     = "payment-server-reloaded"
}

variable "region" {
  default     = "europe-west3"
}

#GKE
variable "gke_cluster_name" {
  default     = "main"
}

variable "gke_cluster_location" {
  default     = "europe-west3"
}

variable "gke_cluster_initial_node_count" {
  default     = "1"
}

variable "gke_cluster_daily_maintenance_window_start_time" {
  default     = "01:00"
}

variable "gke_node_pool_name" {
  default     = "test-pool"
}

variable "gke_node_pool_location" {
  default     = "europe-west2"
}

variable "gke_node_pool_node_count" {
  default     = "1"
}

variable "gke_node_pool_machine_type" {
  default     = "n1-standard-1"
}

variable "gke_node_pool_preemptible" {
  default     = true
}

variable "gke_node_pool_oauth_scopes" {
  default     = [
                  "https://www.googleapis.com/auth/devstorage.read_only",
                  "https://www.googleapis.com/auth/logging.write",
                  "https://www.googleapis.com/auth/monitoring",
                  "https://www.googleapis.com/auth/service.management.readonly",
                  "https://www.googleapis.com/auth/servicecontrol",
                  "https://www.googleapis.com/auth/trace.append"
                ]
}

variable "gke_node_pool_auto_scaling_min_node_count" {
  default     = "1"
}

variable "gke_node_pool_auto_scaling_max_node_count" {
  default     = "3"
}

variable "gke_node_pool_auto_repair" {
  default     = true
}

variable "gke_node_pool_auto_upgrade" {
  default     = true
}

#CLOUDSQL
variable "cloudsql_instance_name" {
  default     = "stash-backend-test-db1"
}

variable "cloudsql_instance_region" {
  default     = "europe-west3"
}

variable "cloudsql_instance_database_version" {
  default     = "POSTGRES_9_6"
}

variable "cloudsql_instance_settings_availability_type" {
  default     = "ZONAL"
}

variable "cloudsql_instance_settings_tier" {
  default     = "db-f1-micro"
}

variable "cloudsql_instance_settings_disk_type" {
  default     = "PD_SSD"
}

variable "cloudsql_instance_settings_disk_size" {
  default     = "10"
}

variable "cloudsql_instance_settings_disk_autoresize" {
  default     = true
}

variable "cloudsql_instance_settings_zone" {
  default     = "europe-west3-a"
}

variable "cloudsql_instance_settings_pricing_plan" {
  default     = "PER_USE"
}

variable "cloudsql_instance_settings_backup_enabled" {
  default     = true
}

variable "cloudsql_instance_settings_backup_start_time" {
  default     = "01:00"
}

variable "cloudsql_database_name" {
  default     = "test-database"
}

variable "cloudsql_sql_user_name" {
  default     = "test-user"
}

variable "cloudsql_merchant_database_name" {
  default   = "test-merchant-database"
}

variable "cloudsql_sql_merchant_user_name" {
  default   = "test-merchant-user"
}

#BACKEND
variable "backend_address_name" {
  default     = "test-address"
}

variable "backend_address_type" {
  default     = "EXTERNAL"
}

variable "backend_address_network_tier" {
  default     = "PREMIUM"
}

variable "backend_zone_name" {
  default     = "test-zone"
}

variable "backend_zone_dns_name" {
  default     = "payment-dev.mblb.net."
}

variable "backend_dns_a_record_name" {
  default     = "test"
}

variable "backend_dns_a_record_ttl" {
  default     = 1800
}

variable "backend_ssl_cert_name" {
  default     = "test-cert"
}

#K8S
variable "k8s_namespace" {
  default     = "dev"
}

variable "k8s_secret_name" {
  default     = "payment-backend-ws"
}

variable "k8s_secret_jwt_secret" {
  default     = "Xn2r5u8x/A?D(G+KbPeShVkYp3s6v9y$B&E)H@McQfTjWnZq4t7w!z%C*F-JaNdR"
}

variable "k8s_secret_notification_user_name" {
  default     = "notifier"
}

variable "k8s_secret_notification_password" {
  default     = "password"
}

variable "k8s_secret_stash_sdk_secret_key" {
  default     = "secret-key"
}

variable "k8s_secret_backend_name" {
  default     = "default-service-account"
}

variable "k8s_secret_backend_account_path" {
  default     = "./creds/backend_account.json"
}