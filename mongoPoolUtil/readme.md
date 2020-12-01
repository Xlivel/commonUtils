配置方式
数据库1
spring.data.mongodb.{beanId}.uri=10.0.147.144:27120,10.0.147.143:27120
spring.data.mongodb.{beanId}.database=lives
spring.data.mongodb.{beanId}.showClass=false
spring.data.mongodb.{beanId}.password=123
spring.data.mongodb.{beanId}.username=vk
数据库2
spring.data.mongodb.{beanId1}.uri=10.0.147.144:27120,10.0.147.143:27120
spring.data.mongodb.{beanId1}.database=lives
spring.data.mongodb.{beanId1}.showClass=false
spring.data.mongodb.{beanId1}.password=123
spring.data.mongodb.{beanId1}.username=vk
数据库3
...

使用方式：
Application启动类添加@EnableMongoPool注解

dao层（例：{beanId}为配置的中对应数据源的字符串）
@Repository
@MongoDbCol("{beanId}")
public class OutlookDataDao extends BaseMongoDaoImpl<OutlookData> {
}


同样的如果我们要操作GridFS的话也可以通过gridFsTemplateName属性来配置gridFsTemplate的名称

配置完之后就可以使用了，使用代码如下：


去掉_class
Spring Data Mongodb在保存数据的时候会自动增加一个_class字段用来关联这条数据的Document实体类，大数据量下会浪费存储空间，我们可以通过下面的配置禁用掉：
spring.data.mongodb.testMongoTemplate.showClass=false

/////////////////////////////////////////////////////////////
更新：1.1
添加处理findById查询不到数据的情况。使用方法findByIdV1;