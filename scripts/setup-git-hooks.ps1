# Git hook'larini etkinlestirir (bir kez calistir)
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

git config core.hooksPath .githooks
Write-Host "Git hook'lari etkin: .githooks/commit-msg" -ForegroundColor Green
Write-Host "Cursor hook'lari: .cursor/hooks.json (otomatik yuklenir)" -ForegroundColor Green
