# 🚀 Guia de Conventional Commits

Este projeto usa **Conventional Commits** para padronizar mensagens de commit.

## 📋 Formato

```
[emoji] tipo(escopo): descrição

corpo (opcional)

rodapé (opcional)
```

## 🏷️ Tipos de commit

| Tipo       | Emoji | Descrição                                   |
| ---------- | ----- | ------------------------------------------- |
| `feat`     | ✨    | Nova funcionalidade                         |
| `fix`      | 🐛    | Correção de bug                             |
| `docs`     | 📝    | Documentação                                |
| `style`    | 💄    | Formatação, estilos (sem mudança de lógica) |
| `refactor` | ♻️    | Refatoração de código                       |
| `perf`     | ⚡    | Melhoria de performance                     |
| `test`     | ✅    | Adição ou correção de testes                |
| `build`    | 📦    | Mudanças no sistema de build                |
| `ci`       | 💚    | Mudanças na integração contínua             |
| `chore`    | 🔧    | Tarefas de manutenção                       |
| `revert`   | ⏪    | Reverter commit anterior                    |

## ✅ Exemplos válidos

```bash
✨ feat: adicionar sistema de login
🐛 fix(auth): corrigir validação de email
📝 docs: atualizar README com instruções
💄 style: formatar código com prettier
♻️ refactor(user): simplificar lógica de validação
⚡ perf: otimizar query do banco de dados
✅ test: adicionar testes para UserService
🔧 chore: configurar husky para git hooks
```

## ❌ Exemplos inválidos

```bash
adicionar login          # sem tipo
feat adicionar login     # sem dois pontos
FEAT: adicionar login    # tipo em maiúsculo
feat: Adicionar login    # descrição com maiúscula
```

## 🎯 Dicas

- Use até 50 caracteres na primeira linha
- Primeira linha com no máximo 4 palavras (recomendação)
- Comece a descrição com letra minúscula
- Não termine a descrição com ponto
- Use emoji no início para melhor visualização
- Para mudanças que quebram compatibilidade, adicione `!` após o tipo: `feat!:`

## 🚫 O que acontece se não seguir?

O commit será **rejeitado** automaticamente pelo git hook, e você verá uma mensagem explicativa com exemplos.
