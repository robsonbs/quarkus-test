# 📚 Materiais de Apresentação

Este diretório contém todos os materiais necessários para a apresentação do projeto **Sistema de Gestão de Notas e Tarefas**.

---

## 📁 Conteúdo

| Arquivo | Descrição | Uso |
|---------|-----------|-----|
| [SLIDES.md](./SLIDES.md) | Apresentação completa em 14 slides | Apresentação principal |
| [DIAGRAMAS.md](./DIAGRAMAS.md) | Diagramas Mermaid da arquitetura | Visualização técnica |
| [GUIA-DEMONSTRACAO.md](./GUIA-DEMONSTRACAO.md) | Roteiro passo-a-passo | Durante a demo |
| [RESUMO-EXECUTIVO.md](./RESUMO-EXECUTIVO.md) | Resumo para entrega | Documentação |

---

## 🚀 Quick Start

### Antes da Apresentação

```bash
# 1. Verificar banco de dados
docker compose ps

# 2. Iniciar banco (se necessário)
docker compose up -d postgres

# 3. Executar testes para confirmar
./mvnw test

# 4. Iniciar aplicação
./mvnw quarkus:dev

# 5. Abrir navegador
open http://localhost:8080
```

### Contas de Demonstração

| Perfil | E-mail | Senha |
|--------|--------|-------|
| 👑 Admin | `admin@example.com` | `123` |
| 👤 User | `user@example.com` | `123` |

---

## 📊 Status do Projeto

### Testes

```
✅ 185 testes passando
✅ 0 falhas
✅ 0 erros
```

### Requisitos

```
✅ RF1 - Autenticação
✅ RF2 - Usuários
✅ RF3 - Perfis
✅ RF4 - Navegação
✅ RF5 - Casos de Uso (Notas + Tarefas)
✅ RF6 - Auditoria
```

---

## 🎯 Dicas para Apresentação

1. **Prepare o ambiente** 5 minutos antes
2. **Teste login** com as duas contas
3. **Crie pelo menos uma nota/tarefa** durante a demo
4. **Mostre a auditoria** para demonstrar rastreabilidade
5. **Execute os testes** no final para impressionar

---

## 📖 Outros Documentos

| Documento | Localização |
|-----------|-------------|
| README completo | [ `/README.md` ](../../README.md) |
| Análise de Requisitos | [ `/docs/analise-requisitos.md` ](../analise-requisitos.md) |
| Plano de Alterações | [ `/docs/plano-alteracoes.md` ](../plano-alteracoes.md) |

---

**Boa apresentação! 🎉**
