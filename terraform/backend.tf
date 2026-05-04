terraform {
  backend "s3" {
    bucket = "asgn-10-terraform-state"
    #for security best practice, I am using a separate dev dir inside bucket as using dev/staging/prod envs is 
    #considered as best practice
    key = "asgn-10/dev/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt = true
  }
}