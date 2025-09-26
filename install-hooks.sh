#!/bin/bash
# Script para instalar hooks do Git no Linux/macOS
# Execute este arquivo após clonar o repositório

echo "🚀 Configurando Git Hooks..."
echo

# Verifica se o diretório .git existe
if [ ! -d ".git" ]; then
    echo "❌ ERRO: Este não é um repositório Git válido!"
    echo "Execute este script na raiz do projeto."
    exit 1
fi

# Cria o diretório hooks se não existir
if [ ! -d ".git/hooks" ]; then
    mkdir -p ".git/hooks"
    echo "📁 Diretório .git/hooks criado"
fi

# Copia o hook commit-msg
if [ -f ".githooks/commit-msg" ]; then
    cp ".githooks/commit-msg" ".git/hooks/commit-msg"
    chmod +x ".git/hooks/commit-msg"
    echo "✅ Hook commit-msg instalado com sucesso!"
else
    echo "❌ ERRO: Arquivo .githooks/commit-msg não encontrado!"
    exit 1
fi

echo
echo "🎉 Git Hooks configurados com sucesso!"
echo
echo "📋 Agora todos os commits devem seguir o padrão Conventional Commits:"
echo "   ✨ feat: nova funcionalidade"
echo "   🐛 fix: correção de bug"  
echo "   📝 docs: documentação"
echo
echo "💡 Consulte o arquivo CONVENTIONAL_COMMITS.md para mais informações."
echo