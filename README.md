# PTDB — Portal de Treinamento Dom Bosco

Aplicação web do Portal de Treinamento Dom Bosco (Engenharia de Software B — 2026/2).

Este commit implementa o **RF06 — Manter vídeos** (CRUD do Administrador) a nível de backend + páginas HTML.

## Stack

- Java 21
- Spring Boot 3.3.5 (Web MVC, Data JPA, Validation, Thymeleaf)
- Banco H2 em memória (dev) — base provisória do RF10
- Maven

## Como executar

```bash
mvn spring-boot:run
```

Acesse `http://localhost:8080/` (redireciona para `/admin/videos`).

Console do H2 (dev): `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:ptdb`, usuário `sa`, sem senha.

## Escopo do RF06 implementado

CRUD completo de vídeos restrito ao perfil Administrador:

- **Cadastro (Create):** título, descrição, categoria, arquivo do vídeo (upload) e data de publicação obrigatórios; imagem de capa (thumbnail) opcional. Todo vídeo cadastrado nasce com status "Aguardando avaliação".
- **Consulta (Read):** listagem com busca por título/descrição e filtros por categoria e status.
- **Edição (Update):** ao editar, o vídeo retorna para "Aguardando avaliação" (RF08). O arquivo é mantido se nenhum novo for enviado.
- **Exclusão (Delete):** sempre lógica (inativação), preservando a integridade referencial com favoritos (RF07) e avaliação (RF08).

Regras de negócio cobertas: campos obrigatórios validados; upload com validação de tipo (vídeo/imagem); armazenamento apenas dos metadados no banco, com o arquivo em disco (RNF05); acesso restrito ao Administrador.

### Rotas

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/admin/videos` | Listagem com filtro/busca |
| GET | `/admin/videos/novo` | Formulário de cadastro |
| POST | `/admin/videos` | Salva cadastro |
| GET | `/admin/videos/{id}/editar` | Formulário de edição |
| POST | `/admin/videos/{id}` | Salva edição |
| POST | `/admin/videos/{id}/excluir` | Exclusão lógica |
| GET | `/admin/midia/videos/{nome}` | Stream do arquivo de vídeo |
| GET | `/admin/midia/thumbnails/{nome}` | Exibe a imagem de capa |

## Arquitetura (camadas — RNF07)

```
web/            HomeController (redirect raiz)
video/          Video, VideoStatus, VideoRepository, VideoService, VideoController, VideoForm, MidiaController
categoria/      Categoria, CategoriaRepository        (stub do RF13/RF10)
storage/        FileStorageService, StorageException   (upload de arquivos)
security/       PerfilUsuario, AdministradorInterceptor (stub do RF01/RF02)
common/         exceções e GlobalExceptionHandler
config/         WebConfig, CargaInicialCategorias
```

## Premissas e stubs (dependências de outros RFs ainda não implementados)

- **RF01/RF02 (login/perfis):** ainda não existem. A restrição de acesso ao Administrador é feita por um interceptor que lê o perfil da sessão. Em dev, `ptdb.dev.auto-admin=true` (em `application.properties`) simula um Administrador autenticado. **Desligar esse flag** quando o RF01/RF02 estiverem prontos.
- **RF10 (banco):** modelagem definitiva pendente. Foram criadas as entidades mínimas `Video` e `Categoria` com H2 em memória.
- **RF13 (categorias):** ainda não há CRUD de categorias. `Categoria` é um stub e algumas categorias são pré-carregadas em dev (`CargaInicialCategorias`).
- **RF07/RF08 (favoritos/avaliação):** a exclusão lógica já preserva a integridade para quando essas entidades existirem.
