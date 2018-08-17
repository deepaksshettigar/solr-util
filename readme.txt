mvn compile
mvn test
mvn package
java -Dlog4j.skipJansi=false -jar target/junit-0.0.1-SNAPSHOT.jar 
mvn -Dlog4j.skipJansi=false spring-boot:run

