variable "project" {
    type = string
    description = "GCP project name"
}

variable "address_name" {
    type = string
    description = "Name of the IP address"
}

variable "address_type" {
    type = string
    description = "Type of the IP address"
}

variable "address_network_tier" {
    type = string
    description = "Network tier of the IP address"
}

variable "zone_name" {
    type = string
    description = "Zone name of the managed DNS"
}

variable "zone_dns_name" {
    type = string
    description = "Name of the managed DNS"
}

variable "dns_a_record_name" {
    type = string
    description = "Name of the A record"
}

variable "dns_a_record_ttl" {
    type = number
    description = "TTL of the A record"
}

variable "ssl_cert_name" {
    type = string
    description = "Name of the SSL cert"
}