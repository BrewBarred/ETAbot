@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Usage:
REM   .\run.bat hi.java
REM   .\run.bat hi.java "https://explv.github.io/?centreX=2798&centreY=3347&centreZ=0&zoom=7"
REM   .\run.bat hi.java ""arg1" "arg2" "arg3""
REM
REM NOTE:
REM - Anything after the first argument is forwarded to the Java program.
REM - If you pass a URL containing '&', you MUST wrap it in quotes or CMD will split it.

if "%~1"=="" (
  echo Usage: run.bat ^<FileName.java^> [args...]
  exit /b 2
)

set "ROOT=%~dp0"
set "SRCROOT=%ROOT%scripts"
set "OUTDIR=%ROOT%out"

REM Capture the first argument (the source filename) BEFORE we SHIFT.
REM After SHIFT, %1/%2/%* will refer to the remaining args (args after file).
set "FILEARG=%~1"

REM Get arg and aggressively remove any quote characters
set "TARGETFILE=%~1"
set "TARGETFILE=%TARGETFILE:"=%"

REM Add .java only if it isn't already there
if /i not "%TARGETFILE:~-5%"==".java" (
  set "TARGETFILE=%TARGETFILE%.java"
)

REM Remove the file argument from the argument list.
REM After this SHIFT:
REM   - %1 becomes the FIRST "extra" argument after the filename
REM   - %* becomes ALL extra arguments (the ones we want to forward into Java)
shift

REM Save "args after file" into a named variable so debug output is not confusing.
REM IMPORTANT:
REM - Using %* here is unreliable in some cases due to batch expansion timing.
REM - Instead, rebuild ARGS from the SHIFTED positional args.
set "ARGS="
:BUILD_ARGS
if "%~1"=="" goto :ARGS_DONE
if defined ARGS (
  set "ARGS=!ARGS! %1"
) else (
  set "ARGS=%1"
)
shift
goto :BUILD_ARGS
:ARGS_DONE

if not exist "%SRCROOT%" (
  echo ERROR: Source folder not found: "%SRCROOT%"
  exit /b 3
)

REM Find real matches only (DIR won't invent fake paths)
set "FOUND="
set /a COUNT=0
for /f "usebackq delims=" %%F in (`dir /b /s /a:-d "%SRCROOT%\%TARGETFILE%" 2^>nul`) do (
  set /a COUNT+=1
  if !COUNT! EQU 1 set "FOUND=%%F"
)

if %COUNT% EQU 0 (
  echo ERROR: Could not find "%TARGETFILE%" under "%SRCROOT%"
  exit /b 4
)

if %COUNT% GTR 1 (
  echo ERROR: Multiple matches for "%TARGETFILE%". Matches:
  for /f "usebackq delims=" %%F in (`dir /b /s /a:-d "%SRCROOT%\%TARGETFILE%" 2^>nul`) do echo   %%F
  exit /b 6
)

for %%N in ("%FOUND%") do set "CLASSNAME=%%~nN"

REM Extract package (if any)
set "PKG="
for /f "usebackq delims=" %%L in ("%FOUND%") do (
  set "LINE=%%L"
  :TRIM_LINE
  if "!LINE:~0,1!"==" " set "LINE=!LINE:~1!" & goto :TRIM_LINE

  if "!LINE:~0,8!"=="package " (
    set "TMP=!LINE:package =!"
    for /f "delims=;" %%P in ("!TMP!") do set "PKG=%%P"
    :TRIM_PKG_TAIL
    if defined PKG if "!PKG:~-1!"==" " set "PKG=!PKG:~0,-1!" & goto :TRIM_PKG_TAIL
    goto :PKG_DONE
  )
)
:PKG_DONE

if defined PKG (
  set "QUALIFIEDCLASS=%PKG%.%CLASSNAME%"
) else (
  set "QUALIFIEDCLASS=%CLASSNAME%"
)

REM Always clean output dir to prevent stale .class files being executed
if exist "%OUTDIR%" rmdir /s /q "%OUTDIR%"
mkdir "%OUTDIR%"

echo ==========================================================
echo Located file path:         %FOUND%
echo Output directory:          %OUTDIR%
REM BEFORE SHIFT (captured):
echo Process (original file):   %FILEARG%
echo Sanitized file:            %TARGETFILE%
REM AFTER SHIFT (captured):
echo Args:                      %ARGS%
echo Run class:                 %QUALIFIEDCLASS%
echo ==========================================================
echo.

javac -encoding UTF-8 ^
  -d "%OUTDIR%" ^
  -sourcepath "%SRCROOT%" ^
  "%FOUND%"

if errorlevel 1 (
  echo.
  echo ERROR: javac failed.
  exit /b 5
)

echo.
REM Forward any extra args into Java.
REM If ARGS is empty, Java runs with no args (same as before).
java -cp "%OUTDIR%" "%QUALIFIEDCLASS%" %ARGS%

exit /b %ERRORLEVEL%
