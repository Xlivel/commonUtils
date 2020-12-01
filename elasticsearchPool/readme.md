配置：
#配置客户端的其他信息
#properties.
# 多个节点以,号分割
spring.data.elasticsearch.cluster-nodes=192.168.15.130.9300,192.168.15.128.9300
# 集群名称
spring.data.elasticsearch.cluster-name=ES
# 是否启用ElasticSearch存储库 默认为true
spring.data.elasticsearch.repositories.enabled=true