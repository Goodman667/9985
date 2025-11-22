param(
  [int]$Port = 8080,
  [string]$Jar = "target/phq9-assessment-0.0.1-SNAPSHOT.jar",
  [switch]$RunTests
)

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

function Stop-PortProcess([int]$p){
  $c = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
  if ($c) { Stop-Process -Id $c.OwningProcess -Force }
}

Stop-PortProcess -p $Port

if ($RunTests) {
  mvn clean package -DskipTests=false
} else {
  mvn package -DskipTests
}

if (!(Test-Path $Jar)) { Write-Error "Jar not found: $Jar"; exit 1 }

Start-Process "http://localhost:$Port/"
java -jar $Jar --server.port=$Port