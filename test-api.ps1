# Freelancer Portal API Test Script
# Run this after restarting your application

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Freelancer Portal API Test Suite" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get
    Write-Host "✓ Health Check: " -ForegroundColor Green -NoNewline
    Write-Host "$($health.status)" -ForegroundColor White
    Write-Host "  Database Status: $($health.components.db.status)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Register a Test User
Write-Host "Test 2: User Registration" -ForegroundColor Yellow
$registerBody = @{
    firstName = "Test"
    lastName = "User"
    email = "testuser$(Get-Random)@example.com"
    password = "TestPassword123!"
    role = "FREELANCER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "✓ Registration successful" -ForegroundColor Green
    Write-Host "  User: $($registerResponse.user.email)" -ForegroundColor Gray
    $testEmail = $registerResponse.user.email
} catch {
    Write-Host "✗ Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $responseBody = $reader.ReadToEnd()
        Write-Host "  Response: $responseBody" -ForegroundColor DarkRed
    }
    $registerResponse = $null
}
Write-Host ""

# Test 3: Login
Write-Host "Test 3: User Login" -ForegroundColor Yellow
if ($registerResponse -and $testEmail) {
    $loginBody = @{
        email = $testEmail
        password = "TestPassword123!"
    } | ConvertTo-Json

    try {
        $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
        Write-Host "✓ Login successful" -ForegroundColor Green
        Write-Host "  Token received: $($loginResponse.token.Substring(0, 20))..." -ForegroundColor Gray
        $token = $loginResponse.token
    } catch {
        Write-Host "✗ Login Failed: $($_.Exception.Message)" -ForegroundColor Red
        $token = $null
    }
} else {
    Write-Host "⊘ Skipped (registration failed)" -ForegroundColor DarkGray
}
Write-Host ""

# Test 4: Get User Profile (Authenticated)
Write-Host "Test 4: Get User Profile (Authenticated)" -ForegroundColor Yellow
if ($token) {
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $profile = Invoke-RestMethod -Uri "$baseUrl/users/me" -Method Get -Headers $headers
        Write-Host "✓ Profile retrieved" -ForegroundColor Green
        Write-Host "  Email: $($profile.email)" -ForegroundColor Gray
        Write-Host "  Name: $($profile.firstName) $($profile.lastName)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ Profile retrieval Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⊘ Skipped (no token available)" -ForegroundColor DarkGray
}
Write-Host ""

# Test 5: Get Projects (Should require authentication)
Write-Host "Test 5: Get Projects (Authenticated)" -ForegroundColor Yellow
if ($token) {
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
        }
        $projects = Invoke-RestMethod -Uri "$baseUrl/projects" -Method Get -Headers $headers
        Write-Host "✓ Projects endpoint accessible" -ForegroundColor Green
        Write-Host "  Projects found: $($projects.Count)" -ForegroundColor Gray
    } catch {
        Write-Host "✗ Projects retrieval Failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⊘ Skipped (no token available)" -ForegroundColor DarkGray
}
Write-Host ""

# Summary
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Test Suite Completed" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Your application is running on: $baseUrl" -ForegroundColor White
Write-Host "For full API documentation, see: API_ENDPOINTS.md" -ForegroundColor White
Write-Host ""
