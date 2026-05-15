$env:JAVA_HOME="E:\ai\jdk-17.0.2"
$env:MAVEN_HOME="E:\ai\apache-maven-3.9.12"
$env:Path="E:\ai\jdk-17.0.2\bin;E:\ai\apache-maven-3.9.12\bin;E:\ai\node-v20.19.4-win-x64;$env:Path"

Write-Host "Java Version:"
java -version

Write-Host "`nMaven Version:"
mvn -version

Write-Host "`nStarting Spring Boot Application..."
cd backend
mvn spring-boot:run