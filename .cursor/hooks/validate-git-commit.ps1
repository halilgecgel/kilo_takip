# Cursor agent git commit oncesi Turkce mesaj kontrolu
$rawInput = ""
if (-not [Console]::IsInputRedirected) {
    Write-Output '{"permission":"allow"}'
    exit 0
}

$rawInput = [Console]::In.ReadToEnd()
if ([string]::IsNullOrWhiteSpace($rawInput)) {
    Write-Output '{"permission":"allow"}'
    exit 0
}

try {
    $payload = $rawInput | ConvertFrom-Json
} catch {
    Write-Output '{"permission":"allow"}'
    exit 0
}

$command = [string]$payload.command
if ($command -notmatch '(?i)\bgit\s+commit\b') {
    Write-Output '{"permission":"allow"}'
    exit 0
}

# -m "...", -m '...' ve HEREDOC govdesindeki metinleri topla
$textToCheck = [System.Collections.Generic.List[string]]::new()
foreach ($match in [regex]::Matches($command, '(?i)-m\s+(?:"([^"]*)"|''([^'']*)'')')) {
    $msg = if ($match.Groups[1].Success) { $match.Groups[1].Value } else { $match.Groups[2].Value }
    if ($msg) { $textToCheck.Add($msg) }
}

if ($command -match "(?s)EOF\r?\n(.+?)\r?\nEOF") {
    $textToCheck.Add($Matches[1])
}

if ($textToCheck.Count -eq 0) {
    # Mesaj gorunmuyorsa git hook yakalar; agent commit'e devam edebilir
    Write-Output '{"permission":"allow"}'
    exit 0
}

$firstLine = ($textToCheck[0] -split "`r?`n")[0].Trim()
$isBad = $false

if ($firstLine -match '^(?i)(feat|fix|chore|docs|refactor|style|test|perf|build|ci|revert)(\([^)]+\))?:') {
    $isBad = $true
}
if ($firstLine -match '^(?i)(Update|Upgrade|Add|Fix|Refactor|Remove|Delete|Implement|Improve|Initial commit|Bump|Migrate|Rename|Create|Set up|Setup)\b') {
    $isBad = $true
}

if ($isBad) {
    $response = @{
        permission    = "deny"
        user_message  = "Commit mesaji Turkce olmali."
        agent_message = "Commit mesajini Turkce yaz. Yasak: feat:/fix:/chore: ve Ingilizce basliklar (Update, Add, Fix vb.). Ornek: Gradle ve bagimliliklar guncellendi"
    } | ConvertTo-Json -Compress
    Write-Output $response
    exit 2
}

Write-Output '{"permission":"allow"}'
exit 0
