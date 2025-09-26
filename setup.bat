@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

echo 🚀 Iniciando setup do projeto KnowHall (Windows)

REM 1. Verifica Java
for /f "tokens=2 delims==" %%v in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /i "java.version"') do set JVER=%%v
set JVER=%JVER:~1,2%
if "%JVER%"=="21" (
  echo ✅ Java 21 detectado
) else (
  echo ⚠️ Versao Java detectada: %JVER% (recomendado: 21)
)

REM 2. Cria .env a partir de .env.example se não existir
if not exist .env (
  if exist .env.example (
    copy .env.example .env >nul
    echo ✅ Arquivo .env criado (revise os valores se necessário)
  ) else (
    echo ⚠️ .env.example não encontrado
  )
) else (
  echo ℹ️  Arquivo .env já existe
)

REM 3. Baixa dependencias Maven offline
if exist mvnw.cmd (
  echo 📦 Preparando dependencias Maven (offline)...
  call mvnw.cmd -q dependency:go-offline >nul 2>nul
) else (
  echo ❌ mvnw.cmd não encontrado
)

REM 4. Instala dependencias Node se existir package.json
if exist package.json (
  where node >nul 2>nul
  if %ERRORLEVEL%==0 (
    echo 📦 Instalando dependencias Node...
    call npm install --no-audit --no-fund
  ) else (
    echo ⚠️ Node não encontrado, pulando dependencias JS
  )
)

REM 5. Instala hooks
if exist install-hooks.bat (
  call install-hooks.bat >nul
  echo ✅ Hooks instalados
) else (
  echo ⚠️ Script install-hooks.bat não encontrado
)

REM 6. Build rápido
if exist mvnw.cmd (
  echo 🔧 Compilando (skip tests)...
  call mvnw.cmd -q clean package -DskipTests
)

echo.
echo ✅ Setup concluído!
echo Próximos passos:
echo   1. Ajuste .env se preciso
echo   2. Rode: mvnw.cmd spring-boot:run
echo   3. (Opcional) npm run format:check

echo Fim.
ENDLOCAL

