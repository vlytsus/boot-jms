# Java Spring Boot project with embedded ActiveMQ JMS provider & distributed JTA transactions manager
Spring Boot with embedded JMS provider & JTA transactions

[Also see detailed description article on Medium.com ...](https://medium.com/@victorlytsus/java-spring-boot-project-with-embedded-activemq-jms-provider-distributed-jta-transactions-f490f914fae2)

![JMS](https://github.com/vlytsus/boot-jms/blob/master/docs/jms.png)

This project contains example of JMS producer-consumer integration with database and transactions.
You can test how @Transactional annotation influences to data commit & rollback
In UserServiceImpl#createUser message is sent to JMS and user is stored in database.

## Testing
There are several endpoints exposed to test transactions from different perspectives. By default project should be configured to not use JTA transaction management (spring-boot-starter-jta-atomikos dependency is not active).
In that case Spring Framework uses it's own local declarative transactions.
Lets call following endpoints in browser to create users user01, user02 and user03:
* http://localhost:8081/users/create?username=user01
* http://localhost:8081/users/transactional/create?username=user02
* http://localhost:8081/users/transactional/with_clone/create?username=user03
* http://localhost:8081/users/all

Last query "users/all" will print all users inserted to the database:
```
user01 : user02 : user03_clone : user03
```
In logs you will also see that JMS consumer has received some messages:
```
##################################
: received payload: <JMS received User : user01>
##################################
: received payload: <JMS received User : user02>
##################################
: received payload: <JMS received User : user03_clone>
##################################
: received payload: <JMS received User : user03>
##################################
```
As you see operation "transactional/with_clone/create" inserts 2 records. It will be used later to demonstrate data rollback.
Now let's modify our queries to not provide any usernames for new users. Which should lead database insertion errors because field username must not be null.

* http://localhost:8081/users/transactional/create
* http://localhost:8081/users/transactional/with_clone/create
* http://localhost:8081/users/all

All create operations are failed and no data inserted into database. Even method "transactional/with_clone/create" has not stored user record with username "_clone". Becasue second insert with null username causes exception and rollback of whole transaction together with inserted "_clone" user.
However, despite database state was rolled back, messages were processed by JMS. Because Spring local transactions have no influence on JMS consumer.

```
 ##################################
: received payload: <JMS received User : null>
 ##################################
 : received payload: <JMS received User : null_clone>
 ##################################
 : received payload: <JMS received User : null>
 ##################################
```
![local transaction](https://github.com/vlytsus/boot-jms/blob/master/docs/local-transaction.png)

If you want to mix Database and JMS transactions together and make trully atomic operations you should handle transactions by some distributed JTA transactions manager.

![distributed transaction](https://github.com/vlytsus/boot-jms/blob/master/docs/distributed%20transaction.png)

In this project is used popular embeddable cloud-native transaction manager - Atomikos.
To activate it you should uncomment Atomikos dependency in pom.xml 
```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jta-atomikos</artifactId>
</dependency>
```
Please rebuild and restart application after that.
If you repeat last test (without usernames) you will notice that JMS messages are not processed. There will be no corresponding logs like "received payload: <JMS received User : null>" because rollback was propagated to JMS by JTA Manager too.

* http://localhost:8081/users/transactional/create
* http://localhost:8081/users/transactional/with_clone/create
* http://localhost:8081/users/all

As result there are no JMS messages from consumer
Try again with normal usernames and you'll see that everything works fine as during first test:

* http://localhost:8081/users/transactional/create?username=user05
* http://localhost:8081/users/transactional/with_clone/create?username=user06
* http://localhost:8081/users/all
```
user05 : user06_clone : user06
```
And JMS consumed messages in logs:
```
 ##################################
: received payload: <JMS received User : user05>
 ##################################
 : received payload: <JMS received User : user06_clone>
 ##################################
 : received payload: <JMS received User : user06>
 ##################################
 ```
 
 # Conclusion
Spring allows to use @Transactional annotations to handle local transactions on JDBC level. Hovewer if you want to have global distributed transactions with several resources like databases & JMS consumers you have to configure JPA Transaction Management Provider. However as you might be noticed distributed transactions are much more slower because of synchronizations.

![local-vs-distributed](https://github.com/vlytsus/boot-jms/blob/master/docs/local-vs-distributed.png)

