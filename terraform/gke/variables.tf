variable "project" {
    type = string
    description = "GCP project name"
}

variable "cluster_name" {
    type = string
    description = "GKE cluster name"
}

variable "cluster_location" {
    type = string
    description = "Location of the resources"
}

variable "cluster_initial_node_count" {
    type = string
    description = "Initial number of nodes in GKE cluster"
}

variable "cluster_daily_maintenance_window_start_time" {
    type = string
    description = "Starting time for maintenance in GMT"
}

variable "node_pool_name" {
    type = string
    description = "Name of the node pool"
}

variable "node_pool_location" {
    type = string
    description = "Location of the node pool"
}

variable "node_pool_node_count" {
    type = string
    description = "Node count per zone"
}

variable "node_pool_machine_type" {
    type = string
    description = "Machine type of each node"
}

variable "node_pool_preemptible" {
    type = bool
    description = "Preemtible flag"
}

variable "node_pool_oauth_scopes" {
    type    = list(string)
    description = "OAUTH scopes definitions"
}

variable "node_pool_auto_scaling_min_node_count" {
    type = string
    description = "Min node count per zone"
}

variable "node_pool_auto_scaling_max_node_count" {
    type = string
    description = "Max node count per zone"
}

variable "node_pool_auto_repair" {
    type = bool
    description = "Auto repair flag"
}

variable "node_pool_auto_upgrade" {
    type = bool
    description = "Auto upgrade flag"
}