# PTDB — Portal de Treinamento Dom Bosco

Aplicação web do Portal de Treinamento Dom Bosco (Engenharia de Software B — 2026/2).

## Stack

- Java 21+
- Spring Boot 3.3.5 (Web MVC, Data JPA, Validation, Thymeleaf)
- Banco H2 em memória (dev)
- Maven (via Maven Wrapper)

## Como executar

No Windows, pelo `cmd`, na pasta do projeto:

```cmd
executar.bat
```

O script verifica o Java (baixa um JDK 21 portátil em `.jdk\` se não houver Java 21+), compila o projeto com o Maven Wrapper, sobe a aplicação e abre o navegador. Para usar outra porta: `executar.bat 8081`. Para parar: `Ctrl + C`.

Alternativa manual (qualquer sistema com Java 21+):

```bash
./mvnw spring-boot:run
```

Acesse `http://localhost:8080/` (redireciona para `/admin/videos`).

Console do H2 (dev): `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:ptdb`, usuário `sa`, sem senha. O banco é recriado a cada execução.

## Funcionalidades

### Gerenciamento de vídeos

CRUD de vídeos restrito ao perfil Administrador, em uma única tela: indicadores (total, aguardando, aprovados, reprovados), filtros, tabela de vídeos e formulário de cadastro/edição em modal.

- **Cadastro:** título, descrição, categoria, vídeo e data de publicação obrigatórios; imagem de capa opcional. O vídeo pode ser um **link externo** (YouTube, Vimeo, Google Drive etc., validado como `http`/`https`) ou um **arquivo enviado**. Para links do YouTube sem capa, a capa do próprio vídeo é usada. Todo vídeo cadastrado nasce com status "Aguardando avaliação".
- **Consulta:** busca por título/descrição e filtros por categoria e status.
- **Edição:** ao editar, o vídeo retorna para "Aguardando avaliação". O arquivo é mantido se nenhum novo for enviado; ao trocar arquivo por link, o arquivo antigo é removido.
- **Exclusão:** sempre lógica (inativação), preservando a integridade referencial com favoritos e avaliações.

Visibilidade: vídeos aprovados e ativos podem ser vistos por todos; pendentes e reprovados apenas por administradores e avaliadores. A consulta de aprovados (`VideoService.listarAprovados`) já está disponível para o catálogo.

O banco guarda apenas os metadados; arquivos enviados ficam em disco, na pasta `uploads/` (configurável em `ptdb.storage.location`).

### Identidade visual

Folha de estilo única (`src/main/resources/static/css/ptdb.css`) com paleta, tipografia (Segoe UI) e componentes padronizados, e layout responsivo:

| Largura | Navegação | Indicadores | Tabela de gestão | Grade de cards (catálogo) |
| --- | --- | --- | --- | --- |
| ≥ 1280px | barra lateral | 4 colunas | tabela | 3 colunas |
| 1024–1279px | barra lateral | 4 colunas | tabela | 2 colunas |
| 768–1023px | barra superior | 2 colunas | tabela | 2 colunas |
| < 768px | barra superior | 2 colunas | cartões empilhados | 1 coluna |

Novas telas reutilizam o layout base em `templates/fragments/layout.html`:

```html
<head th:replace="~{fragments/layout :: head('Título da tela')}"></head>
<body>
<div class="app">
    <aside th:replace="~{fragments/layout :: navegacao('item-ativo')}"></aside>
    <main class="conteudo">...</main>
</div>
</body>
```

### Rotas

| Método | Rota | Descrição |
| --- | --- | --- | --- | --- |
| GET | `/admin/videos` | Listagem com indicadores, filtro e busca |
| GET | `/admin/videos/novo` | Listagem com o modal de cadastro aberto |
| POST | `/admin/videos` | Salva cadastro |
| GET | `/admin/videos/{id}/editar` | Listagem com o modal de edição aberto |
| POST | `/admin/videos/{id}` | Salva edição |
| POST | `/admin/videos/{id}/excluir` | Exclusão lógica |
| GET | `/midia/videos/{nome}` | Arquivo de vídeo enviado (respeita a visibilidade) |
| GET | `/midia/capas/{nome}` | Imagem de capa (respeita a visibilidade) |

Erros 403, 404 e 500 são exibidos com o layout do portal (`templates/error.html`).

## Testes

```bash
./mvnw test
```

Cobrem validação de link/arquivo, visibilidade por perfil, consulta de aprovados, indicadores, fluxo do modal e páginas de erro.

## Arquitetura (camadas)

```
web/            HomeController (redirect raiz)
video/          Video, VideoStatus, OrigemVideo, LinkVideo, VideoRepository, VideoService, VideoController, VideoForm, MidiaController
categoria/      Categoria, CategoriaRepository
storage/        FileStorageService, StorageException
security/       PerfilUsuario, UsuarioSessao, AdministradorInterceptor
common/         exceções e GlobalExceptionHandler
config/         WebConfig, CargaInicialCategorias
```

## Pendências e soluções provisórias

- **Login e perfis:** ainda não existem. O acesso ao gerenciamento é controlado por um interceptor que lê o perfil da sessão. Em dev, `ptdb.dev.auto-admin=true` (em `application.properties`) simula um Administrador autenticado. **Desligar esse flag** quando a autenticação estiver pronta.
- **Banco de dados:** modelagem definitiva pendente. Por enquanto há as entidades mínimas `Video` e `Categoria` em H2 em memória.
- **Categorias:** ainda não há CRUD de categorias; algumas são pré-carregadas em dev (`CargaInicialCategorias`).
