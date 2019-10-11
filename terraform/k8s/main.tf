data "google_client_config" "default" {}

provider "kubernetes" {
    load_config_file = false
    host = "https://${var.cluster_endpoint}"
    token = "${data.google_client_config.default.access_token}"
    cluster_ca_certificate = "${base64decode(var.cluster_ca_certificate)}"
}

resource "kubernetes_namespace" "namespace" {
    metadata {
        name = "${var.namespace}"
    }
}

resource "kubernetes_secret" "secret" {
    metadata {
        name      = "${var.secret_name}"
        namespace = "${var.namespace}"
    }

    data = {
        DB_PASSWORD           = "${var.secret_db_password}"
        DB_USERNAME           = "${var.secret_db_user_name}"
        JWT_SECRET            = "${var.secret_jwt_secret}"
        NOTIFICATION_PASSWORD = "${var.secret_notification_password}"
        NOTIFICATION_USERNAME = "${var.secret_notification_user_name}"
        STASH_SDK_SECRETKEY   = "${var.secret_stash_sdk_secret_key}"
    }
}

resource "kubernetes_secret" "secret_backend" {
    metadata {
        name      = "${var.secret_backend_name}"
        namespace = "${var.namespace}"
    }

    data = {
        "key.json" = "${filebase64("${var.secret_backend_account_path}")}"
    }
}