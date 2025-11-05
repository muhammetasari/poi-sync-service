# POI Sync Service - Endpoint Test Script
# ==========================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "POI SYNC SERVICE - ENDPOINT TESTLERI" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 1. HEALTH CHECK
Write-Host "1. HEALTH CHECK" -ForegroundColor Yellow
Write-Host "GET /actuator/health" -ForegroundColor Gray
curl.exe -s http://localhost:8080/actuator/health | ConvertFrom-Json | ConvertTo-Json
Start-Sleep -Seconds 2

# 2. ACTUATOR INFO
Write-Host "`n2. ACTUATOR INFO" -ForegroundColor Yellow
Write-Host "GET /actuator/info" -ForegroundColor Gray
curl.exe -s http://localhost:8080/actuator/info
Start-Sleep -Seconds 2

# 3. NEARBY SEARCH (İzmir - Alsancak Restoranlar)
Write-Host "`n3. NEARBY SEARCH - Restoranlar" -ForegroundColor Yellow
Write-Host "GET /api/places/nearby?lat=38.4192&lng=27.1287&radius=2000&type=restaurant" -ForegroundColor Gray
curl.exe -s "http://localhost:8080/api/places/nearby?lat=38.4192&lng=27.1287&radius=2000&type=restaurant" | ConvertFrom-Json | ConvertTo-Json -Depth 5
Start-Sleep -Seconds 3

# 4. NEARBY SEARCH (İzmir - Alsancak Cafeler)
Write-Host "`n4. NEARBY SEARCH - Cafeler" -ForegroundColor Yellow
Write-Host "GET /api/places/nearby?lat=38.4192&lng=27.1287&radius=1500&type=cafe" -ForegroundColor Gray
curl.exe -s "http://localhost:8080/api/places/nearby?lat=38.4192&lng=27.1287&radius=1500&type=cafe" | ConvertFrom-Json | ConvertTo-Json -Depth 5
Start-Sleep -Seconds 3

# 5. TEXT SEARCH (İzmir Konak)
Write-Host "`n5. TEXT SEARCH" -ForegroundColor Yellow
Write-Host "GET /api/places/text-search?query=izmir+konak+restoranlar" -ForegroundColor Gray
curl.exe -s "http://localhost:8080/api/places/text-search?query=izmir+konak+restoranlar" | ConvertFrom-Json | ConvertTo-Json -Depth 5
Start-Sleep -Seconds 3

# 6. TEXT SEARCH WITH LOCATION BIAS
Write-Host "`n6. TEXT SEARCH WITH LOCATION BIAS" -ForegroundColor Yellow
Write-Host "GET /api/places/text-search?query=starbucks&lat=38.4192&lng=27.1287&radius=5000" -ForegroundColor Gray
curl.exe -s "http://localhost:8080/api/places/text-search?query=starbucks&lat=38.4192&lng=27.1287&radius=5000" | ConvertFrom-Json | ConvertTo-Json -Depth 5
Start-Sleep -Seconds 3

# 7. PLACE DETAILS (önce bir place ID alalım)
Write-Host "`n7. PLACE DETAILS (Örnek Place ID gerekli)" -ForegroundColor Yellow
Write-Host "İlk önce bir place ID bulalım..." -ForegroundColor Gray
$response = curl.exe -s "http://localhost:8080/api/places/text-search?query=starbucks+izmir&maxResults=1" | ConvertFrom-Json
$placeId = $response.places[0].id

if ($placeId) {
    Write-Host "Place ID bulundu: $placeId" -ForegroundColor Green
    Write-Host "GET /api/places/details/$placeId" -ForegroundColor Gray
    curl.exe -s "http://localhost:8080/api/places/details/$placeId" | ConvertFrom-Json | ConvertTo-Json -Depth 5
} else {
    Write-Host "Place ID bulunamadı!" -ForegroundColor Red
}
Start-Sleep -Seconds 3

# 8. LOCATION SYNC (Async - Arka planda çalışır)
Write-Host "`n8. LOCATION SYNC - MongoDB'ye kaydet" -ForegroundColor Yellow
Write-Host "POST /api/sync/locations?lat=38.4192&lng=27.1287&radius=3000&type=restaurant" -ForegroundColor Gray
curl.exe -X POST -s "http://localhost:8080/api/sync/locations?lat=38.4192&lng=27.1287&radius=3000&type=restaurant"
Write-Host "`nNot: Bu işlem arka planda çalışır. Logları kontrol edin: docker-compose logs -f api" -ForegroundColor Cyan
Start-Sleep -Seconds 2

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "TEST TAMAMLANDI!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Logları izlemek için:" -ForegroundColor Yellow
Write-Host "docker-compose logs -f api`n" -ForegroundColor Gray
