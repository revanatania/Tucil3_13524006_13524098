@echo off
setlocal

for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
set "BIN_DIR=%~dp0"
set "TARGET_JAR=%PROJECT_ROOT%\target\icepath-solver-1.0.0.jar"
set "JAR_PATH=%BIN_DIR%IcePathSolver.jar"

if not exist "%JAR_PATH%" (
  if not exist "%TARGET_JAR%" (
    echo [INFO] JAR belum ada. Menjalankan build Maven...
    pushd "%PROJECT_ROOT%"
    call mvn -q -DskipTests package
    set "BUILD_ERROR=%ERRORLEVEL%"
    popd

    if not "%BUILD_ERROR%"=="0" (
      echo [ERROR] Build gagal. Pastikan JDK 17+ dan Maven terpasang.
      pause
      exit /B %BUILD_ERROR%
    )
  )

  if exist "%TARGET_JAR%" (
    copy /Y "%TARGET_JAR%" "%JAR_PATH%" >nul
  )
)

if not exist "%JAR_PATH%" (
  echo [ERROR] File JAR tidak ditemukan: "%JAR_PATH%"
  pause
  exit /B 1
)

start "" javaw -jar "%JAR_PATH%"
if errorlevel 1 (
  echo [WARN] javaw gagal, mencoba java biasa...
  java -jar "%JAR_PATH%"
  if errorlevel 1 (
    echo [ERROR] Aplikasi gagal dijalankan.
    pause
    exit /B 1
  )
)

exit /B 0
