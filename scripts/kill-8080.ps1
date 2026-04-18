$port = 8080
$process = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($process) { 
    Stop-Process -Id $process.OwningProcess -Force -PassThru
    Write-Host 'Stopped process on port 8080' 
} else { 
    Write-Host 'No process found on port 8080' 
}
