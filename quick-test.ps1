# Quick Registration Test
# Copy and paste these commands one by one in PowerShell

# 1. First, make sure your app is restarted to apply CORS fix

# 2. Test Registration
$body = @{
    firstName = "John"
    lastName = "Doe"
    email = "john.doe.test@example.com"
    password = "SecurePass123!"
    role = "FREELANCER"
} | ConvertTo-Json

Write-Host "Testing Registration..." -ForegroundColor Cyan
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/auth/register" -Method Post -Body $body -ContentType "application/json"
    Write-Host "✓ SUCCESS! Registration worked!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Response:" -ForegroundColor Yellow
    $result | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ FAILED!" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    # Try to get detailed error
    if ($_.ErrorDetails.Message) {
        Write-Host "Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
