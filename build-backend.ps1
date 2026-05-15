$env:JAVA_HOME = 'E:\ai\jdk-17.0.2'
$env:Path = "E:\ai\jdk-17.0.2\bin;$env:Path"
Remove-Item 'C:\me\workhome\maven\mavenRepositories362\org\apache\maven\plugins\maven-surefire-plugin\3.5.2\maven-surefire-plugin-3.5.2.pom.part.lock' -Force -ErrorAction SilentlyContinue
cd C:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend\backend
mvn package -DskipTests -q 2>&1 | Select-Object -Last 8
