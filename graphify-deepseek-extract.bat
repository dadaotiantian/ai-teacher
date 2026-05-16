@echo off
setlocal

cd /d "%~dp0"

set "GRAPHIFY_TARGET=%~1"
if "%GRAPHIFY_TARGET%"=="" (
    set "GRAPHIFY_TARGET=."
)

if not exist "%GRAPHIFY_TARGET%" (
    echo Graphify target path does not exist: %GRAPHIFY_TARGET%
    exit /b 1
)

set "DEEPSEEK_API_KEY="
set /p DEEPSEEK_API_KEY=Enter DeepSeek API Key: 

if "%DEEPSEEK_API_KEY%"=="" (
    echo DeepSeek API Key is required.
    exit /b 1
)

set "OLLAMA_BASE_URL=https://api.deepseek.com/v1"
set "OLLAMA_API_KEY=%DEEPSEEK_API_KEY%"
set "OLLAMA_MODEL=deepseek-chat"

echo Extracting graph from: %GRAPHIFY_TARGET%
graphify extract "%GRAPHIFY_TARGET%" --backend ollama --model deepseek-chat --out .
exit /b %ERRORLEVEL%
