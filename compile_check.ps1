$env:JAVA_HOME = "E:\ai\jdk-17.0.2"
$env:MAVEN_HOME = "E:\ai\apache-maven-3.9.12"
$env:Path = "E:\ai\jdk-17.0.2\bin;E:\ai\apache-maven-3.9.12\bin;" + $env:Path

Set-Location "C:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend\backend"
mvn compile -B 2>&1 | Select-Object -Last 20