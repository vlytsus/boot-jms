# boot-jms
Spring Boot with embedded JMS provider & JTA transactions

This project contains example of JMS producer-consumer integration with database and transactions.
You can test how @Transactional annotation influences to data commit & rollback
In UserServiceImpl#createUser message is sent to JMS and user is stored in database.

## Testing
There are several test endpoints exposed to test transactions from different perspectives. By default project should be configured to not use JPA Transaction Management (spring-boot-starter-jta-atomikos dependency is not active).
In that case Spring Framework uses it's own local declarative transactions.
Lets call following endpoints in browser:
* http://localhost:8081/users/create?username=user01
* http://localhost:8081/users/transactional/create?username=user02
* http://localhost:8081/users/transactional/with_clone/create?username=user03
* http://localhost:8081/users/all

Lat query will print all users inserted to the database:
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
Now let's modify our queries to avoid sending username (which is required)

* http://localhost:8081/users/transactional/create
* http://localhost:8081/users/transactional/with_clone/create
* http://localhost:8081/users/all

All create operations are failed and no data inserted into database. Even method "transactional/with_clone/create" not inserted "_clone" user because another query failed and made rollback.
However, messages were processed by JMS. Because Spring local transactions have no influence on JMS consumer.

```
 ##################################
: received payload: <JMS received User : null>
 ##################################
 : received payload: <JMS received User : null_clone>
 ##################################
 : received payload: <JMS received User : null>
 ##################################
```
If you want to mix Database and JMS transactions together and make trully atomic operations you should handle transactions by some distributed JTA transactions manager.
In this project is used popular embeddable cloud-native transaction manager - Atomikos.
To activate you should uncomment Atomikos dependency in pom.xml 
```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jta-atomikos</artifactId>
</dependency>
```
Please rebuild and restart application after that.
If you repeat last test without usernames you will notice that JMS messages are not processed because rollback was propagated to JMS by JTA Manager.

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
And in logs:
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
Spring allows to use @Transactional annotations to handle database transactions on JDBC level. Hovewer if you want to use global distributed transactions with several resources like database & jms you have to configure JPA Transaction Management Provider

