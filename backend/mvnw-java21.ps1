$javaCommand = Get-Command java -ErrorAction Stop
$javaHome = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
$prevEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$version = & (Join-Path $javaHome 'bin\java.exe') -version 2>&1 | Out-String
$ErrorActionPreference = $prevEAP
if ($version -notmatch 'version "21\.') {
  throw "ATELIER requiere Java 21. El java del PATH es: $version"
}
if (Test-Path "$PSScriptRoot\.env") {
  Get-Content "$PSScriptRoot\.env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#') -and $line.Contains('=')) {
      $parts = $line.Split('=', 2)
      $k = $parts[0].Trim()
      $v = $parts[1].Trim()
      if ($k -and -not [System.Environment]::GetEnvironmentVariable($k, 'Process')) {
        [System.Environment]::SetEnvironmentVariable($k, $v, 'Process')
      }
    }
  }
}
$env:JAVA_HOME = $javaHome
& (Join-Path $PSScriptRoot 'mvnw.cmd') @args
exit $LASTEXITCODE
