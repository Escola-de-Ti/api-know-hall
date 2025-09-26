# 🎓 KnowHall

Sistema de gestão do conhecimento desenvolvido com Spring Boot.

## 🚀 Configuração Inicial

### Pré-requisitos

- Java 11+
- PostgreSQL
- Git

### 🔧 Configuração do Ambiente

1. **Clone o repositório:**

   ```bash
   git clone https://github.com/willyantomaz/knowhall.git
   cd knowhall
   ```

2. **Configure as variáveis de ambiente:**
   Crie um arquivo `.env` na raiz do projeto:

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

Este projeto utiliza **Conventional Commits** para padronizar mensagens de commit.

**Formato obrigatório:**

```
[emoji] tipo(escopo): descrição
```

**Exemplos:**

- ✨ `feat: adicionar sistema de login`
- 🐛 `fix(auth): corrigir validação de email`
- 📝 `docs: atualizar README`
- 🔧 `chore: configurar banco de dados`

> ⚠️ **Importante**: Commits que não seguirem este padrão serão automaticamente rejeitados.

**Para mais detalhes, consulte:** [`CONVENTIONAL_COMMITS.md`](CONVENTIONAL_COMMITS.md)

## 🏃‍♂️ Executar o Projeto

```bash
./mvnw spring-boot:run
```

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças seguindo o padrão de commits
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT.
