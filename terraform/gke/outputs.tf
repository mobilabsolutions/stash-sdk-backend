output "cluster_endpoint" {
    value       = "${google_container_cluster.main_cluster.endpoint}"
    description = "Endpoint for accessing the master node"
}

output "cluster_ca_certificate" {
    value = "${google_container_cluster.main_cluster.master_auth.0.cluster_ca_certificate}"
    description = "Certificate of the main cluster"
}