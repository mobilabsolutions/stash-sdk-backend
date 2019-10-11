terraform {
  backend "gcs" {
    credentials = "./creds/terraform_account.json"
    bucket = "stash-terraform-states"
    prefix = "stash"
  }
}