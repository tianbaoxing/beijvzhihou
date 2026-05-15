$env:Path = "E:\ai\jdk-17.0.2\bin;E:\ai\node-v20.19.4-win-x64;E:\ai\apache-maven-3.9.12\bin;" + $env:Path
$env:JAVA_HOME = "E:\ai\jdk-17.0.2"

Write-Host "=== 环境验证 ==="
java -version 2>&1 | Select-Object -First 1
node --version
mvn -version 2>&1 | Select-Object -First 1

Write-Host ""
Write-Host "=== 编译后端 ==="
cd C:\Users\conca\.qclaw\workspace\.worktrees\beijvzhihou-backend\backend
mvn package -DskipTests -q 2>&1
if ($LASTEXITCODE -ne 0) { mvn package -DskipTests 2>&1 | Select-Object -Last 10 }
