resource "google_sql_database_instance" "main" {
    name             = "${var.instance_name}"
    project          = "${var.project}"
    region           = "${var.instance_region}"
    database_version = "${var.instance_database_version}"

    settings {
        availability_type = "${var.instance_settings_availability_type}"
        tier              = "${var.instance_settings_tier}"
        disk_type         = "${var.instance_settings_disk_type}"
        disk_size         = "${var.instance_settings_disk_size}"
        disk_autoresize   = "${var.instance_settings_disk_autoresize}"

        ip_configuration {
            require_ssl  = true
            ipv4_enabled = true
        }

        location_preference {
            zone = "${var.instance_settings_zone}"
        }

        pricing_plan  = "${var.instance_settings_pricing_plan}"

        backup_configuration {
            enabled            = "${var.instance_settings_backup_enabled}"
            start_time         = "${var.instance_settings_backup_start_time}" # GMT
        }
    }
}

resource "google_sql_database" "database" {
    name      = "${var.database_name}"
    instance  = "${google_sql_database_instance.main.name}"
    charset   = "UTF8"
    collation = "en_US.UTF8"
}

resource "google_sql_user" "user" {
    instance = "${google_sql_database_instance.main.name}"
    name     = "${var.sql_user_name}"
    password = "${random_password.password.result}"
}

resource "random_password" "password" {
    length = 16
    special = false
}

resource "google_sql_database" "merchant_database" {
    name      = "${var.database_merchant_name}"
    instance  = "${google_sql_database_instance.main.name}"
    charset   = "UTF8"
    collation = "en_US.UTF8"
}

resource "google_sql_user" "merchant_user" {
    instance = "${google_sql_database_instance.main.name}"
    name     = "${var.sql_merchant_user_name}"
    password = "${random_password.password.result}"
}