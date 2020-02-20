# boot-jms
Spring Boot with embedded JMS

This project contain example of JMS producer-consumer integration with database and transactions.
You can test how @Transactional annotation influences to data commit & rollback

In UserServiceImpl#createUser message is sent to JMS and user is stored in database.
If you use default transaction management and there are several database inserts in one transactional method then all succesfull inserts will be rolled back if at least one failed.
However JMS messages will be delivered succesfully even if they are enclosed in the same transactional method.

To rollback JMS messages procesing you have to configure distributed transaction management service. In this example you can uncomment Atomikos dependency in pom.xml to do that.
'''
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jta-atomikos</artifactId>
</dependency>
'''

Immediately after that (please rebuild + restart applicaion) you will see that JMS consumer will stop accept messages if transaction was failed uppon message producing.
To simulate transaction error you have to create UserEntity without username
