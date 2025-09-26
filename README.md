# 🎓 KnowHall

Sistema de gestão do conhecimento desenvolvido com Spring Boot.

## ⚡ Quick Start

Clonou o repositório e quer tudo pronto rápido?

1. Clone:
   ```bash
   git clone https://github.com/willyantomaz/knowhall.git
   cd knowhall
   ```
2. Rode o script de setup (cria .env se faltar, instala dependências, hooks e prepara build):
   - Linux/macOS:
     ```bash
     chmod +x setup.sh
     ./setup.sh
     ```
   - Windows (CMD ou Powershell):
     ```cmd
     setup.bat
     ```
3. Ajuste o arquivo `.env` (criado a partir de `.env.example` se não existia).
4. Suba a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
5. (Opcional) Verifique formatação e lint:
   ```bash
   npm run format:check
   npm run lint
   ```

> Se preferir seguir passo a passo manual, veja a seção abaixo.

## 🚀 Configuração Inicial

### Pré-requisitos

- Java 21
- PostgreSQL
- Git
- (Opcional) Node.js >= 18 para formatação automática

### 🔧 Configuração do Ambiente

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/willyantomaz/knowhall.git
   cd knowhall
   ```

2. **Configure as variáveis de ambiente:**
   Crie um arquivo `.env` na raiz do projeto (ou copie de `.env.example`).

   ```env
   USER_DATABASE=postgres
   PASSWORD_DATABASE=sua_senha_aqui
   ```

3. **Instale os Git Hooks (IMPORTANTE):**

   **Windows:**

   ```cmd
   install-hooks.bat
   ```

   **Linux/macOS:**

   ```bash
   chmod +x install-hooks.sh
   ./install-hooks.sh
   ```

### 📋 Padrão de Commits

Este projeto utiliza **Conventional Commits**.

Formato:

```
[emoji] tipo(escopo): descrição
```

Exemplos: `feat: ...`, `fix(auth): ...`, `docs: ...`
Commits fora do padrão são rejeitados. Leia [`CONVENTIONAL_COMMITS.md`](CONVENTIONAL_COMMITS.md).

### 🧹 Formatação (Resumo)

- Usamos **Prettier** (+ plugin para Java) e `.editorconfig`.
- Hook `pre-commit`: formata arquivos staged automaticamente.
- Hook `commit-msg`: valida Conventional Commits.
- Rodar manualmente:
  ```bash
  npm install        # primeira vez
  npm run format     # aplicar
  npm run format:check  # só verificar
  ```
  Se o hook não rodar, reinstale: `install-hooks.bat` ou `./install-hooks.sh`.

### 🔍 Lint (ESLint)

- Config flat em `eslint.config.cjs` (ESLint v9+).
- Hook `pre-commit` também roda ESLint (--fix) em arquivos JS/TS.
- Ignorados definidos na chave `ignores` (o `.eslintignore` é legado).
- Comandos:
  ```bash
  npm run lint      # verificar
  npm run lint:fix  # corrigir automaticamente
  ```

### 🏃‍♂️ Executar o Projeto

```bash
./mvnw spring-boot:run
```

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch (`git checkout -b feature/minha-feature`)
3. Commit seguindo o padrão
4. Push (`git push origin feature/minha-feature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.
