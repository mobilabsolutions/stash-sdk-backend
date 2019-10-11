resource "google_container_cluster" "main_cluster" {
    name        = "${var.cluster_name}"
    project     = "${var.project}"
    location    = "${var.cluster_location}"

    remove_default_node_pool = true
    initial_node_count       = "${var.cluster_initial_node_count}"

    monitoring_service = "monitoring.googleapis.com/kubernetes"
    logging_service    = "logging.googleapis.com/kubernetes"

    maintenance_policy {
        daily_maintenance_window {
            start_time = "${var.cluster_daily_maintenance_window_start_time}" # GMT
        }
    }

    master_auth {
        username = ""
        password = ""

        client_certificate_config {
            issue_client_certificate = false
        }
    }
}

resource "google_container_node_pool" "main_node_pool" {
    name       = "${var.node_pool_name}"
    project    = "${var.project}"
    location   = "${var.node_pool_location}"
    cluster    = "${google_container_cluster.main_cluster.name}"
    node_count = "${var.node_pool_node_count}"

    node_config {
        preemptible  = "${var.node_pool_preemptible}"
        machine_type = "${var.node_pool_machine_type}"

        metadata = {
            disable-legacy-endpoints = "true"
        }

        oauth_scopes = "${var.node_pool_oauth_scopes}"
    }

    autoscaling {
        min_node_count = "${var.node_pool_auto_scaling_min_node_count}"
        max_node_count = "${var.node_pool_auto_scaling_max_node_count}"
    }

    management {
        auto_repair  = "${var.node_pool_auto_repair}"
        auto_upgrade = "${var.node_pool_auto_upgrade}"
    }

}