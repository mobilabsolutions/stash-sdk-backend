provider "google" {
  credentials = "${file("./creds/terraform_account.json")}"
  project     = "${var.project}"
  region      = "${var.region}"
}

module "gke" {
  source                                      = "./gke"
  project                                     = "${var.project}"
  cluster_name                                = "${var.gke_cluster_name}"
  cluster_location                            = "${var.gke_cluster_location}"
  cluster_initial_node_count                  = "${var.gke_cluster_initial_node_count}"
  cluster_daily_maintenance_window_start_time = "${var.gke_cluster_daily_maintenance_window_start_time}"
  node_pool_name                              = "${var.gke_node_pool_name}"
  node_pool_location                          = "${var.gke_node_pool_location}"
  node_pool_node_count                        = "${var.gke_node_pool_node_count}"
  node_pool_machine_type                      = "${var.gke_node_pool_machine_type}"
  node_pool_preemptible                       = "${var.gke_node_pool_preemptible}"
  node_pool_oauth_scopes                      = "${var.gke_node_pool_oauth_scopes}"
  node_pool_auto_scaling_min_node_count       = "${var.gke_node_pool_auto_scaling_min_node_count}"
  node_pool_auto_scaling_max_node_count       = "${var.gke_node_pool_auto_scaling_max_node_count}"
  node_pool_auto_repair                       = "${var.gke_node_pool_auto_repair}"
  node_pool_auto_upgrade                      = "${var.gke_node_pool_auto_upgrade}"
}

module "cloudsql" {
  source                               = "./cloudsql"
  project                              = "${var.project}"
  instance_name                        = "${var.cloudsql_instance_name}"
  instance_region                      = "${var.cloudsql_instance_region}"
  instance_database_version            = "${var.cloudsql_instance_database_version}"
  instance_settings_availability_type  = "${var.cloudsql_instance_settings_availability_type}"
  instance_settings_tier               = "${var.cloudsql_instance_settings_tier}"
  instance_settings_disk_type          = "${var.cloudsql_instance_settings_disk_type}"
  instance_settings_disk_size          = "${var.cloudsql_instance_settings_disk_size}"
  instance_settings_disk_autoresize    = "${var.cloudsql_instance_settings_disk_autoresize}"
  instance_settings_zone               = "${var.cloudsql_instance_settings_zone}"
  instance_settings_pricing_plan       = "${var.cloudsql_instance_settings_pricing_plan}"
  instance_settings_backup_enabled     = "${var.cloudsql_instance_settings_backup_enabled}"
  instance_settings_backup_start_time  = "${var.cloudsql_instance_settings_backup_start_time}"
  database_name                        = "${var.cloudsql_database_name}"
  sql_user_name                        = "${var.cloudsql_sql_user_name}"
  database_merchant_name               = "${var.cloudsql_merchant_database_name}"
  sql_merchant_user_name               = "${var.cloudsql_sql_merchant_user_name}"
}

module "backend" {
  source                 = "./backend"
  project                = "${var.project}"
  address_name           = "${var.backend_address_name}"
  address_type           = "${var.backend_address_type}"
  address_network_tier   = "${var.backend_address_network_tier}"
  zone_name              = "${var.backend_zone_name}"
  zone_dns_name          = "${var.backend_zone_dns_name}"
  dns_a_record_name      = "${var.backend_dns_a_record_name}"
  dns_a_record_ttl       = "${var.backend_dns_a_record_ttl}"
  ssl_cert_name          = "${var.backend_ssl_cert_name}"
}

module "k8s" {
  source                        = "./k8s"
  cluster_endpoint              = "${module.gke.cluster_endpoint}"
  cluster_ca_certificate        = "${module.gke.cluster_ca_certificate}"
  namespace                     = "${var.k8s_namespace}"
  secret_name                   = "${var.k8s_secret_name}"
  secret_db_user_name           = "${module.cloudsql.db_user_name}"
  secret_db_password            = "${module.cloudsql.db_password}"
  secret_jwt_secret             = "${var.k8s_secret_jwt_secret}"
  secret_notification_user_name = "${var.k8s_secret_notification_user_name}"
  secret_notification_password  = "${var.k8s_secret_notification_password}"
  secret_stash_sdk_secret_key   = "${var.k8s_secret_stash_sdk_secret_key}"
  secret_backend_name           = "${var.k8s_secret_backend_name}"
  secret_backend_account_path   = "${var.k8s_secret_backend_account_path}"
}