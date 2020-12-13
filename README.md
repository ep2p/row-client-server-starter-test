## How to start

- Build the project using maven: `maven clean package -DskipTests`
- Run 2 instances on separate ports:
  - instance one on port 8080:
  ```
  java -jar target/row-cs-starter-test-0.0.1-SNAPSHOT.jar
  ```
  - instance two on port 8081:
  ```
  java -jar target/row-cs-starter-test-0.0.1-SNAPSHOT.jar --server.port=8081
  ```
- head to your browser and enter: `http://localhost:8080/api/run`