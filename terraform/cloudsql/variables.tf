variable "project" {
    type = string
    description = "GCP project name"
}

variable "instance_name" {
    type = string
    description = "Name of the Cloud SQL instance"
}

variable "instance_region" {
    type = string
    description = "Region of the Cloud SQL instance"
}

variable "instance_database_version" {
    type = string
    description = "Database version of the Cloud SQL instance"
}

variable "instance_settings_availability_type" {
    type = string
    description = "Availability type of the Cloud SQL instance"
}

variable "instance_settings_tier" {
    type = string
    description = "Tier of the Cloud SQL instance"
}

variable "instance_settings_disk_type" {
    type = string
    description = "Disk type of the Cloud SQL instance"
}

variable "instance_settings_disk_size" {
    type = string
    description = "Disk size of the Cloud SQL instance"
}

variable "instance_settings_disk_autoresize" {
    type = bool
    description = "Autoresize flag of the Cloud SQL instance"
}

variable "instance_settings_zone" {
    type = string
    description = "Zone of the Cloud SQL instance"
}

variable "instance_settings_pricing_plan" {
    type = string
    description = "Pricing plan of the Cloud SQL instance"
}

variable "instance_settings_backup_enabled" {
    type = bool
    description = "Backup flag of the Cloud SQL instance"
}

variable "instance_settings_backup_start_time" {
    type = string
    description = "Starting time for backup of the Cloud SQL instance in GMT"
}

variable "database_name" {
    type = string
    description = "Name of the database"
}

variable "sql_user_name" {
    type = string
    description = "Database user name"
}

variable "database_merchant_name" {
    type = string
    description = "Name of the merchnt database"
}

variable "sql_merchant_user_name" {
    type = string
    description = "Merchant database user name"
}