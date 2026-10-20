# PTDB — Portal de Treinamento Dom Bosco

Aplicação web do Portal de Treinamento Dom Bosco (Engenharia de Software B — 2026/2).

Este repositório implementa os requisitos **RF06, RF07, RF08, RF09, RF11, RF12 e RF13**.

## Stack

- Java 21+
- Spring Boot 3.3.5 (Web MVC, Data JPA, Thymeleaf)
- Banco H2 em memória
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

Acesse `http://localhost:8080/`.

Console do H2: `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:ptdb`, usuário `sa`, sem senha. O banco é recriado a cada execução.

### Perfil provisório

Login (RF01) e perfis (RF02) não fazem parte deste escopo. Para testar as telas de cada perfil, o portal começa como **Administrador** e o menu lateral tem a opção **Trocar perfil**, que alterna entre três usuários de teste:

| Perfil | Usuário | Tela inicial |
| --- | --- | --- |
| Administrador | Administrador do Portal | `/admin` |
| Avaliador | Rafael Lima | `/avaliacao` |
| Aluno | Bruno Alves | `/catalogo` |

## Requisitos implementados

| Requisito | Onde |
| --- | --- |
| RF06 Manter vídeos | `/admin/videos`: cadastrar, consultar, editar e excluir vídeos (título, descrição, autor, categoria, link ou arquivo, capa e data de publicação). |
| RF07 Favoritos | Coração nos cards e na página do vídeo (aluno) e página `/favoritos`. Sem duplicados; ao excluir o vídeo, os favoritos dele são removidos. |
| RF08 Tela do avaliador | `/avaliacao`: fila, player, parecer obrigatório e decisão (aprovar, reprovar ou solicitar ajustes). Histórico em `/avaliacao/historico`. |
| RF09 Área administrativa | `/admin` (indicadores, atividade recente e fila de avaliação), mais gestão de vídeos, categorias e FAQ. |
| RF11 CSS | Folha única `static/css/ptdb.css`, responsiva. |
| RF12 FAQ | `/faq` (acordeão agrupado por tema, busca e filtro por tema) e `/admin/faq` (cadastro pelo administrador). |
| RF13 Vídeos por categoria | `/catalogo`: lista de categorias com a contagem de vídeos aprovados; ao escolher uma, mostra só os vídeos dela. |

### Fluxo de um vídeo

1. Administrador cadastra → **Aguardando avaliação**.
2. Avaliador decide, sempre com parecer → **Aprovado**, **Reprovado** ou **Ajustes solicitados**.
3. Só vídeos aprovados aparecem no catálogo e podem ser favoritados.
4. Qualquer edição do administrador devolve o vídeo para **Aguardando avaliação**.

### Acesso por perfil

| Rotas | Perfil |
| --- | --- |
| `/catalogo`, `/videos/**`, `/faq` | Todos |
| `/favoritos/**` | Aluno |
| `/avaliacao/**` | Avaliador |
| `/admin/**` | Administrador |

Com o perfil errado, o usuário recebe a página 403 do portal.

## Rotas

| Método | Rota | Descrição |
| --- | --- | --- |
| GET | `/` | Leva para a tela inicial do perfil atual |
| POST | `/perfil` | Troca o perfil provisório |
| GET | `/catalogo` | Catálogo por categoria (`termo`, `categoriaId`) |
| GET | `/videos/{id}` | Página do vídeo |
| GET | `/favoritos` | Meus favoritos |
| POST | `/favoritos/{videoId}` | Marca/desmarca favorito |
| GET | `/avaliacao`, `/avaliacao/historico` | Tela do avaliador |
| POST | `/avaliacao/{videoId}` | Registra a decisão |
| GET | `/faq` | Perguntas frequentes |
| GET | `/admin` | Painel administrativo |
| GET/POST | `/admin/videos/**` | Gerenciar vídeos |
| GET/POST | `/admin/categorias/**` | Gerenciar categorias |
| GET/POST | `/admin/faq/**` | Gerenciar FAQ |
| GET | `/midia/videos/{nome}`, `/midia/capas/{nome}` | Arquivos enviados (respeitam a visibilidade) |

## Testes

```bash
./mvnw test
```

Cobrem permissões por perfil, gestão de vídeos, catálogo e filtro por categoria, favoritos, avaliação (incluindo ajustes), categorias, FAQ, painel e páginas de erro.

## Organização do código

```
security/   PerfilUsuario, UsuarioSessao, UsuarioLogado, PerfilController (perfil provisório), interceptors
usuario/    Usuario, UsuarioRepository
catalogo/   CatalogoController (catálogo por categoria e página do vídeo)
video/      Video, VideoService, VideoController (admin), MidiaController, LinkVideo
avaliacao/  Avaliacao, AvaliacaoService, AvaliacaoController, DecisaoAvaliacao
favorito/   Favorito, FavoritoService, FavoritoController
categoria/  Categoria, CategoriaService, CategoriaController
faq/        ItemFaq, FaqService, FaqController, FaqAdminController
painel/     PainelService, PainelController e classes dos indicadores
config/     WebConfig (regras de acesso) e cargas iniciais (usuários de teste, categorias, FAQ)
storage/    FileStorageService
common/     exceções e GlobalExceptionHandler
web/        Redirecionamento
```

## Fora do escopo deste repositório

RF01 (login), RF02 (perfis), RF03 (tela inicial com cabeçalho e rodapé), RF04 (contador de visualizações), RF05 (banco de perfis e visualizações em MySQL) e RF10 (estrutura de banco de vídeos e favoritos) não fazem parte deste escopo. O seletor de perfil e a tabela mínima de usuários existem só para permitir testar RF07 e RF08 até o login ficar pronto.
