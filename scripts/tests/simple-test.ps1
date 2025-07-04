Write-Host 'Starting 15 Round Test' -ForegroundColor Cyan
$pass = 0
for ($i = 1; $i -le 15; $i++) {
  $ok = $true
  Write-Host "Round $i..." -NoNewline
  try {
    $h = Invoke-RestMethod "http://localhost:8080/actuator/health"
    $u = "t$(Get-Random)"
    $user = @{username=$u; email="$u@test.com"; password="password123"; realName="Test"; phoneNumber="13800000001"}
    $r = Invoke-RestMethod "http://localhost:8080/api/users/register" -Method POST -Body ($user|ConvertTo-Json) -Headers @{"Content-Type"="application/json"}
    $q = Invoke-RestMethod "http://localhost:8080/api/users/$($r.id)"
    Invoke-RestMethod "http://localhost:8080/api/users/$($r.id)" -Method DELETE
    Write-Host " PASS" -ForegroundColor Green
    $pass++
  } catch {
    Write-Host " FAIL" -ForegroundColor Red
  }
  Start-Sleep -Milliseconds 200
}
Write-Host "Result: $pass/15 rounds passed ($(($pass/15*100))%)" -ForegroundColor $(if($pass -eq 15){"Green"}else{"Red"})
