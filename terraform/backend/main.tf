resource "google_compute_address" "ip_address" {
    name         = "${var.address_name}"
    address_type = "${var.address_type}"
    network_tier = "${var.address_network_tier}"
}

resource "google_dns_managed_zone" "env_dns_zone" {
    name     = "${var.zone_name}"
    project  = "${var.project}"
    dns_name = "${var.zone_dns_name}"
}

resource "google_dns_record_set" "A" {
    name         = "${var.dns_a_record_name}.${google_dns_managed_zone.env_dns_zone.dns_name}"
    managed_zone = "${google_dns_managed_zone.env_dns_zone.name}"
    type         = "A"
    ttl          = "${var.dns_a_record_ttl}"

    rrdatas      = ["${google_compute_address.ip_address.address}"]
}

resource "google_compute_managed_ssl_certificate" "default" {
    provider  = "google-beta"
    project   = "${var.project}"
    name      = "${var.ssl_cert_name}"

    managed {
        domains = ["${var.dns_a_record_name}.${google_dns_managed_zone.env_dns_zone.dns_name}"]
    }
}