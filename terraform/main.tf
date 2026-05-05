
resource "aws_vpc" "main_vpc" {
    cidr_block = "10.0.0.0/16"

    tags = {
      Project = "asgn-10"
    }
}


resource "aws_subnet" "main_public_subnet" {
    vpc_id = aws_vpc.main_vpc.id
    cidr_block = "10.0.1.0/24"
    availability_zone = "us-east-1a"
    map_public_ip_on_launch = false

    tags = {
      Project = "asgn-10"
    }
}


resource "aws_internet_gateway" "main_igw" {
    vpc_id = aws_vpc.main_vpc.id

    tags = {
      Project = "asgn-10"
    }
}


resource "aws_route_table" "main_route_table" {
    vpc_id = aws_vpc.main_vpc.id

    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_internet_gateway.main_igw.id
    }

    tags = {
      Project = "asgn-10"
    }

}

resource "aws_route_table_association" "main_association" {
    subnet_id = aws_subnet.main_public_subnet.id
    route_table_id = aws_route_table.main_route_table.id
}

resource "aws_security_group" "main_security_group" {
    vpc_id = aws_vpc.main_vpc.id

    ingress {
        from_port = 22
        to_port = 22
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        from_port = 6443    #for K3s Cluster deployment
        to_port = 6443
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        from_port = 30004
        to_port = 30004
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        from_port = 30005
        to_port = 30005
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    ingress {
        from_port = 30006
        to_port = 30006
        protocol = "tcp"
        cidr_blocks = ["0.0.0.0/0"]
    }

    egress {
        from_port = 0
        to_port = 0
        protocol = "-1" #Allow all no restriction at all
        cidr_blocks = ["0.0.0.0/0"]
    }

    tags = {
      Project = "asgn-10"
    }

}

resource "aws_instance" "main_instance" {
    ami = "ami-05cf1e9f73fbad2e2"
    instance_type = "m7i-flex.large"

    subnet_id = aws_subnet.main_public_subnet.id
    vpc_security_group_ids = [aws_security_group.main_security_group.id]
    key_name = "asgn-10-key"
    #associate_public_ip_address = true

    root_block_device {     #Added this bcoz of prometheus as it needs a lot of mem and was corrupting the cluster - causing 
        volume_size = 30    # pod eviction and preventing the scheduling of new pods
        volume_type = "gp3"
    }


    tags = {
      Project = "asgn-10"
    }
  
}

resource "aws_eip" "main_eip" {
    instance = aws_instance.main_instance.id

    tags = {
        Project = "asgn-10"
    }

}

output "Instance_Public_IP" {
    #value = aws_instance.main_instance.public_ip
    value = aws_eip.main_eip.public_ip
}