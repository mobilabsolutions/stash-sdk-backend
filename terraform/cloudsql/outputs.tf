output "db_user_name" {
    value       = "${google_sql_user.user.name}"
    description = "User name of DB user"
}

output "db_password" {
    value       = "${google_sql_user.user.password}"
    description = "Password of DB user"
}