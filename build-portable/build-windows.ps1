# Builds the Windows portable package (jpackage app-image) from the project root.
# Handles common local pitfalls: locating Git Bash, Temurin JDKs without jmods,
# and JAVA_HOME paths that contain spaces.
#
# Usage (from repo root or this folder):
#   .\build-portable\build-windows.ps1
#   .\build-portable\build-windows.ps1 -RunTests
#   .\build-portable\build-windows.ps1 -Jdk "C:\Program Files\Eclipse Adoptium\jdk-25..." -Bash "C:\Program Files\Git\bin\bash.exe"

param(
	[string]$Jdk = $env:JAVA_HOME,
	[string]$Bash,
	[switch]$RunTests,
	[switch]$SkipJmodsDownload
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $ScriptDir "..")).Path
Set-Location $Root

function Find-GitBash {
	param([string]$Preferred)
	# Prefer explicit overrides, then common Git for Windows install locations, then PATH.
	$candidates = @(
		$Preferred,
		$env:BASH_EXECUTABLE,
		(Join-Path $env:LOCALAPPDATA "Programs\Git\bin\bash.exe"),
		"C:\Program Files\Git\bin\bash.exe",
		"C:\Program Files (x86)\Git\bin\bash.exe"
	) | Where-Object { $_ -and $_.Trim() -ne "" }
	foreach ($c in $candidates) {
		if (Test-Path -LiteralPath $c) {
			return (Resolve-Path -LiteralPath $c).Path
		}
	}
	$fromPath = Get-Command bash.exe -ErrorAction SilentlyContinue
	if ($fromPath) {
		return $fromPath.Source
	}
	return $null
}

function Ensure-JdkPath {
	param([string]$JdkHome)
	if (-not $JdkHome -or -not (Test-Path -LiteralPath $JdkHome)) {
		throw "JAVA_HOME / -Jdk is missing or invalid: '$JdkHome'"
	}
	$resolved = (Resolve-Path -LiteralPath $JdkHome).Path
	$javaExe = Join-Path $resolved "bin\java.exe"
	$jpackageExe = Join-Path $resolved "bin\jpackage.exe"
	if (-not (Test-Path -LiteralPath $javaExe)) {
		throw "java.exe not found under '$resolved'"
	}
	if (-not (Test-Path -LiteralPath $jpackageExe)) {
		throw "jpackage.exe not found under '$resolved' (need a full JDK, not a JRE)"
	}
	return $resolved
}

function Ensure-Jmods {
	param(
		[string]$JdkHome,
		[switch]$SkipDownload
	)
	$jmodsDir = Join-Path $JdkHome "jmods"
	$existing = @(Get-ChildItem -LiteralPath $jmodsDir -Filter "*.jmod" -ErrorAction SilentlyContinue)
	if ($existing.Count -gt 0) {
		Write-Host "Found $($existing.Count) jmod(s) in $jmodsDir"
		return
	}
	if ($SkipDownload) {
		throw "JDK has no jmods at '$jmodsDir'. Temurin 24+ ships them separately; re-run without -SkipJmodsDownload or install jdk+jmods."
	}

	$verLine = & (Join-Path $JdkHome "bin\java.exe") -version 2>&1 | Select-Object -First 1
	if ($verLine -notmatch 'version "([^"]+)"') {
		throw "Cannot parse Java version from: $verLine"
	}
	$javaVersion = $Matches[1] # e.g. 25.0.2
	$build = $null
	$full = & (Join-Path $JdkHome "bin\java.exe") -XshowSettings:properties -version 2>&1 |
		Where-Object { $_ -match 'java.runtime.version\s*=\s*(.+)$' } |
		Select-Object -First 1
	if ($full -match 'java.runtime.version\s*=\s*(\S+)') {
		$runtime = $Matches[1] # e.g. 25.0.2+10
		if ($runtime -match '^([0-9.]+)\+(\d+)') {
			$javaVersion = $Matches[1]
			$build = $Matches[2]
		}
	}
	if (-not $build) {
		throw "Cannot determine Temurin build number for jmods download (need e.g. 25.0.2+10)."
	}

	$tag = "jdk-${javaVersion}%2B${build}"
	$url = "https://api.adoptium.net/v3/binary/version/$tag/windows/x64/jmods/hotspot/normal/eclipse?project=jdk"
	$zip = Join-Path $env:TEMP "OpenJDK-jmods_${javaVersion}_${build}.zip"
	$extract = Join-Path $env:TEMP "jmods-extract-${javaVersion}-${build}"

	Write-Host "Downloading Temurin jmods for ${javaVersion}+${build} ..."
	Write-Host "  $url"
	curl.exe -L --fail --retry 3 --retry-delay 2 -o $zip $url
	if ($LASTEXITCODE -ne 0) {
		throw "Failed to download jmods (curl exit $LASTEXITCODE)"
	}

	if (Test-Path -LiteralPath $extract) {
		Remove-Item -LiteralPath $extract -Recurse -Force
	}
	Expand-Archive -LiteralPath $zip -DestinationPath $extract -Force
	$jmodFile = Get-ChildItem -LiteralPath $extract -Recurse -Filter "*.jmod" | Select-Object -First 1
	if (-not $jmodFile) {
		throw "No .jmod files found in downloaded archive"
	}
	$srcDir = $jmodFile.Directory.FullName
	New-Item -ItemType Directory -Force -Path $jmodsDir | Out-Null
	Copy-Item -Path (Join-Path $srcDir "*.jmod") -Destination $jmodsDir -Force
	$count = @(Get-ChildItem -LiteralPath $jmodsDir -Filter "*.jmod").Count
	Write-Host "Installed $count jmod(s) into $jmodsDir"
}

# --- main --------------------------------------------------------------------------

Write-Host "Project root: $Root"

$bashPath = Find-GitBash -Preferred $Bash
if (-not $bashPath) {
	throw "Git Bash (bash.exe) not found. Install Git for Windows or pass -Bash <path>."
}
Write-Host "Bash: $bashPath"

$jdkHome = Ensure-JdkPath -JdkHome $Jdk
Write-Host "JDK:  $jdkHome"
Ensure-Jmods -JdkHome $jdkHome -SkipDownload:$SkipJmodsDownload

$mvnArgs = @(
	"-B", "clean", "package", "-Pportable",
	"-Dbash.executable=$bashPath"
)
if (-not $RunTests) {
	$mvnArgs += "-DskipTests"
}

# Previous portable runs lock Karnak.exe / run-out.txt and break "mvn clean".
function Clear-PortableOutput {
	Get-Process -Name "Karnak" -ErrorAction SilentlyContinue | ForEach-Object {
		Write-Host "Stopping Karnak.exe (PID $($_.Id))..."
		Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
	}
	Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | Where-Object {
		$_.CommandLine -and ($_.CommandLine -match 'karnak-windows|Karnak\\Karnak\.exe')
	} | ForEach-Object {
		Write-Host "Stopping PID $($_.ProcessId) ($($_.Name))..."
		Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
	}
	Start-Sleep -Seconds 1
	$target = Join-Path $Root "target"
	Get-ChildItem -LiteralPath $target -Directory -Filter "karnak-windows-*" -ErrorAction SilentlyContinue | ForEach-Object {
		$old = $_.FullName
		$bak = Join-Path $target ("_old_pkg_" + (Get-Date -Format "yyyyMMddHHmmss") + "_" + $_.Name)
		Write-Host "Moving locked package aside: $($_.Name)"
		try {
			Rename-Item -LiteralPath $old -NewName (Split-Path $bak -Leaf) -ErrorAction Stop
			Remove-Item -LiteralPath $bak -Recurse -Force -ErrorAction SilentlyContinue
		} catch {
			Write-Host "Warning: could not remove '$old' ($_). Close Karnak/Explorer windows using that folder and retry."
		}
	}
}

Clear-PortableOutput

Write-Host ""
Write-Host "Running: mvn $($mvnArgs -join ' ')"
Write-Host ""

$env:JAVA_HOME = $jdkHome
& mvn @mvnArgs
if ($LASTEXITCODE -ne 0) {
	Write-Host ""
	Write-Host "Hint: close any running portable Karnak (and folders under target\karnak-windows-*), then retry."
	throw "Maven portable build failed with exit code $LASTEXITCODE"
}

$pkg = Get-ChildItem -LiteralPath (Join-Path $Root "target") -Directory -Filter "karnak-windows-*" |
	Sort-Object LastWriteTime -Descending |
	Select-Object -First 1
if ($pkg) {
	Write-Host ""
	Write-Host "Portable package ready:"
	Write-Host "  $($pkg.FullName)"
	Write-Host "Run:  $($pkg.FullName)\run.bat"
} else {
	Write-Host "Build finished, but no target\karnak-windows-* folder was found."
}
