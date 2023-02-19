module "msk-cluster" {
  source  = "angelabad/msk-cluster/aws"

  cluster_name    = "BoehmerKafkaCluster"
  instance_type   = "kafka.t3.small"
  number_of_nodes = 2
  client_subnets  = module.vpc.public_subnets
  kafka_version   = "2.6.2"

  client_authentication_unauthenticated_enabled = "true"

  server_properties = {
    "auto.create.topics.enable"  = "true"
    "allow.everyone.if.no.acl.found" = "true"
    "default.replication.factor" = "2"
    
  }
}
