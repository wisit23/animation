$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$ffmpegDirectory = Join-Path $projectDirectory "tools\ffmpeg"
$downloadUrl = "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip"
$archivePath = Join-Path ([System.IO.Path]::GetTempPath()) "codex-animation-ffmpeg.zip"

try {
    Write-Host "Downloading portable FFmpeg..."
    Invoke-WebRequest -Uri $downloadUrl -OutFile $archivePath

    New-Item -ItemType Directory -Path $ffmpegDirectory -Force | Out-Null
    Write-Host "Extracting FFmpeg..."
    Expand-Archive -LiteralPath $archivePath -DestinationPath $ffmpegDirectory -Force
    Remove-Item -LiteralPath $archivePath

    $ffmpegExecutable = Get-ChildItem -LiteralPath $ffmpegDirectory -Recurse -Filter "ffmpeg.exe" |
        Select-Object -First 1 -ExpandProperty FullName
} catch {
    Write-Host "Portable download failed. Installing FFmpeg with winget..."
    winget install --id Gyan.FFmpeg --exact --accept-package-agreements `
        --accept-source-agreements --silent --disable-interactivity

    $wingetPackages = Join-Path $env:LOCALAPPDATA "Microsoft\WinGet\Packages"
    $ffmpegExecutable = Get-ChildItem -LiteralPath $wingetPackages -Recurse -Filter "ffmpeg.exe" |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $ffmpegExecutable) {
    throw "ffmpeg.exe was not found after extraction."
}

Write-Host "FFmpeg is ready: $ffmpegExecutable"
Write-Host "Run: javac Assignment1_studentID_yourPairID.java ExportAnimationToMp4.java"
Write-Host "Then: java ExportAnimationToMp4"
