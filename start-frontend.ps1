$env:NODE_PATH="E:\ai\node-v20.19.4-win-x64\node_modules"
$env:Path="E:\ai\node-v20.19.4-win-x64;$env:Path"

Write-Host "Node Version:"
node -v

Write-Host "`nNPM Version:"
npm -v

Write-Host "`nStarting Frontend Dev Server..."
npm run dev