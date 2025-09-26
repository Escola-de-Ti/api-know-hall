#!/bin/bash
set -e

echo "🚀 Iniciando setup do projeto KnowHall"

# 1. Verifica Java
if command -v java >/dev/null 2>&1; then
  JAVA_VERSION=$(java -version 2>&1 | head -n1 | sed -E 's/.*version "([0-9]+).*/\1/')
  if [ "$JAVA_VERSION" != "21" ]; then
    echo "⚠️  Java $JAVA_VERSION detectado. Recomendado: 21"
  else
    echo "✅ Java 21 detectado"
  fi
else
  echo "❌ Java não encontrado no PATH. Instale Java 21 antes de continuar."
  exit 1
fi

# 2. Cria .env se não existir
if [ ! -f .env ]; then
  if [ -f .env.example ]; then
    cp .env.example .env
    echo "✅ Arquivo .env criado a partir de .env.example (ajuste os valores se necessário)."
  else
    echo "⚠️  .env.example não encontrado; pule esta etapa."
  fi
else
  echo "ℹ️  Arquivo .env já existe."
fi

# 3. Baixa dependências Maven offline
echo "📦 Baixando dependências Maven (offline cache)..."
./mvnw -q dependency:go-offline || echo "⚠️  Não foi possível preparar totalmente o cache offline (pode continuar)."

# 4. Instala dependências Node (se package.json existir e Node instalado)
if [ -f package.json ]; then
  if command -v node >/dev/null 2>&1; then
    echo "📦 Instalando dependências Node..."
    npm install --no-audit --no-fund
  else
    echo "⚠️  Node não encontrado; pulando instalação de dependências JS (opcional)."
  fi
fi

# 5. Instala hooks
if [ -f install-hooks.sh ]; then
  bash install-hooks.sh
else
  echo "⚠️  Script install-hooks.sh não encontrado."
fi

# 6. Verificação rápida de build
echo "🔧 Compilando para verificação rápida..."
./mvnw -q clean test -DskipTests || echo "⚠️  Build encontrou problemas (verifique manualmente)."

echo "✅ Setup concluído!"
echo "Próximos passos:"
echo "  1. Ajuste o arquivo .env conforme necessário"
echo "  2. Rode: ./mvnw spring-boot:run"
echo "  3. (Opcional) npm run format:check"

