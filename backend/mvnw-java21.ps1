$ErrorActionPreference = 'Stop'
$javaCommand = Get-Command java -ErrorAction Stop
$javaHome = Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
$version = & (Join-Path $javaHome 'bin\java.exe') -version 2>&1 | Out-String
if ($version -notmatch 'version "21\.') {
  throw "ATELIER requiere Java 21. El java del PATH es: $version"
}
$env:JAVA_HOME = $javaHome
& (Join-Path $PSScriptRoot 'mvnw.cmd') @args
exit $LASTEXITCODE
