@echo off
REM Script para instalar hooks do Git no Windows
REM Execute este arquivo após clonar o repositório

echo 🚀 Configurando Git Hooks...
echo.

REM Verifica se o diretório .git existe
if not exist ".git" (
    echo ❌ ERRO: Este não é um repositório Git válido!
    echo Execute este script na raiz do projeto.
    pause
    exit /b 1
)

REM Cria o diretório hooks se não existir
if not exist ".git\hooks" (
    mkdir ".git\hooks"
    echo 📁 Diretório .git\hooks criado
)

REM Copia o hook commit-msg
if exist ".githooks\commit-msg" (
    copy ".githooks\commit-msg" ".git\hooks\commit-msg" >nul
    echo ✅ Hook commit-msg instalado com sucesso!
) else (
    echo ❌ ERRO: Arquivo .githooks\commit-msg não encontrado!
    pause
    exit /b 1
)

echo.
echo 🎉 Git Hooks configurados com sucesso!
echo.
echo 📋 Agora todos os commits devem seguir o padrão Conventional Commits:
echo    ✨ feat: nova funcionalidade
echo    🐛 fix: correção de bug  
echo    📝 docs: documentação
echo.
echo 💡 Consulte o arquivo CONVENTIONAL_COMMITS.md para mais informações.
echo.
pause