# Google Cloud Storage – Upload de imagens

A API VendaLume envia e carrega imagens (logo da empresa, foto de produto, etc.) no **Google Cloud Storage (GCS)**. O upload é opcional: se o GCS não estiver configurado, o endpoint de upload retorna 503.

---

## 1. Criar projeto e bucket no Google Cloud

### 1.1 Criar projeto (se ainda não tiver)

1. Acesse [Google Cloud Console](https://console.cloud.google.com/).
2. No seletor de projeto, clique em **Novo projeto**.
3. Nome: por exemplo `vendalume-prod`.
4. Anote o **ID do projeto** (ex.: `vendalume-prod-123456`).

### 1.2 Ativar a API do Cloud Storage

1. No menu, vá em **APIs e serviços** → **Biblioteca**.
2. Pesquise por **Cloud Storage API**.
3. Clique em **Ativar**.

### 1.3 Criar o bucket

1. Menu **Storage** → **Buckets** (ou [storage.cloud.google.com](https://console.cloud.google.com/storage)).
2. **Criar bucket**.
3. Nome global único, ex.: `vendalume-uploads-prod`.
4. Região: escolha uma próxima dos seus usuários (ex.: `southamerica-east1`).
5. **Tipo de armazenamento**: Standard.
6. **Controle de acesso**:  
   - Para URLs públicas diretas (recomendado para logos/fotos):  
     - Crie o bucket e depois (passo 1.4) torne os objetos públicos.  
   - Ou use apenas **IAM** (sem “público”) e, no futuro, URLs assinadas.
7. Conclua a criação.

### 1.4 Desbloquear a “Prevenção de acesso público” (obrigatório para URLs públicas)

Na aba **Permissões** do bucket você vê algo como: **Acesso público: Não público**, com o texto de que o acesso público está sendo impedido e **Principais com acesso restrito ao bucket: allUsers, allAuthenticatedUsers**. Enquanto isso estiver ativo, você **não consegue** adicionar `allUsers`. É preciso **remover a prevenção** primeiro.

**Opção A – Pelo Console**

1. Na mesma aba **Permissões**, no **card "Acesso público"**, procure um **link** ou **botão** tipo **"Permitir acesso público"** ou **"Allow public access"** e clique; confirme na caixa de diálogo.
2. Depois use **Conceder acesso** e adicione o principal **`allUsers`** com a função **Leitor de objetos do Cloud Storage**.

Se esse link **não aparecer** (por exemplo por política da organização), use a **Opção B**.

**Opção B – Pela linha de comando (gcloud)** (use esta se no Console não aparecer o botão)

1. Instale o [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) e faça login: `gcloud auth login`; defina o projeto: `gcloud config set project SEU_PROJECT_ID`.
2. Remova a prevenção no bucket:

```bash
gcloud storage buckets update gs://SEU_BUCKET_NAME --no-public-access-prevention
```

Substitua `SEU_BUCKET_NAME` pelo nome do bucket (ex.: `vendalume-uploads-prod`).

3. Volte ao Console → aba **Permissões** do bucket → **Conceder acesso** → principal **`allUsers`** → função **Leitor de objetos do Cloud Storage**.

**Permissão necessária:** papel **Storage Admin** (`roles/storage.admin`) no projeto ou no bucket.

### 1.5 Deixar o bucket público para leitura (opcional)

Para que as URLs retornadas pela API (`https://storage.googleapis.com/SEU_BUCKET/...`) abram no navegador sem login:

1. Abra o bucket → aba **Permissões**.
2. **Conceder acesso**.
3. Novos principais: `allUsers`.
4. Função: **Leitor de objetos do Cloud Storage**.
5. Salve (aviso de “público” – confirme se for o desejado).

Se não fizer o passo 1.4, ao tentar adicionar `allUsers` aparecerá que a prevenção de acesso público está aplicada. Se não fizer o 1.5, as URLs só funcionarão para quem tiver permissão no projeto (ou use depois URLs assinadas).

---

## 2. Credenciais (Service Account)

A API usa uma **conta de serviço** para enviar arquivos ao GCS.

### 2.1 Criar a conta de serviço

1. **IAM e administração** → **Contas de serviço**.
2. **Criar conta de serviço**.
3. Nome: ex. `vendalume-upload`.
4. **Criar e continuar**.
5. Função: **Cloud Storage** → ** ** (permite criar/editar objetos no bucket).
6. Conclua.

### 2.2 Chave JSON

1. Na lista de contas de serviço, clique na que você criou.
2. Aba **Chaves** → **Adicionar chave** → **Criar nova chave** → **JSON**.
3. O arquivo será baixado (ex.: `vendalume-prod-xxxx.json`).  
   **Guarde em local seguro e não commite no Git.**

### 2.3 Dar permissão no bucket para a conta de serviço

1. Abra o bucket → **Permissões**.
2. **Conceder acesso**.
3. Principal: e-mail da conta de serviço (ex.: `vendalume-upload@vendalume-prod.iam.gserviceaccount.com`).
4. Função: **Desenvolvedor de objetos do Cloud Storage** (ou **Admin de objetos** se preferir).
5. Salve.

---

## 3. Configuração na API (variáveis de ambiente)

Use as variáveis abaixo (ou equivalentes no `application.yml` / profile).

| Variável | Obrigatório | Descrição |
|----------|-------------|-----------|
| `GCS_ENABLED` | Sim (para usar GCS) | `true` para ativar upload no GCS. |
| `GCS_BUCKET_NAME` | Sim | Nome do bucket (ex.: `vendalume-uploads-prod`). |
| `GCP_PROJECT_ID` | Recomendado | ID do projeto (ex.: `vendalume-prod-123456`). |
| `GCS_CREDENTIALS_JSON` | Um dos três* | **JSON completo** da chave da conta de serviço em uma única string (recomendado em containers/cloud). |
| `GCS_CREDENTIALS_PATH` | Um dos três* | Caminho absoluto do arquivo JSON da conta de serviço. |
| `GOOGLE_APPLICATION_CREDENTIALS` | Um dos três* | Caminho do JSON (padrão do Google). |

\* A aplicação usa, **nesta ordem**: (1) `GCS_CREDENTIALS_JSON` se estiver preenchido, (2) `GCS_CREDENTIALS_PATH` se estiver preenchido, (3) credenciais padrão do ambiente (`GOOGLE_APPLICATION_CREDENTIALS` ou Application Default Credentials).

### Opção recomendada: JSON em uma variável (`GCS_CREDENTIALS_JSON`)

Você envia o **conteúdo inteiro do arquivo JSON** da conta de serviço em uma única variável. Não precisa de arquivo no servidor; ideal para Docker, Kubernetes, Cloud Run, etc.

**Exemplo (Linux/macOS)** – use aspas simples para o JSON inteiro:

```bash
export GCS_ENABLED=true
export GCS_BUCKET_NAME=vendalume-uploads-prod
export GCP_PROJECT_ID=vendalume-prod-123456
export GCS_CREDENTIALS_JSON='{"type":"service_account","project_id":"seu-projeto",...}'
```

O valor de `GCS_CREDENTIALS_JSON` é o **mesmo conteúdo** do arquivo `.json` que você baixou da conta de serviço (uma linha ou várias; a API aceita os dois).

**Em Kubernetes:** crie um Secret com o JSON e exponha como variável de ambiente, ou use `valueFrom.secretKeyRef` apontando para a chave que contém o JSON.

**Em arquivo .env (desenvolvimento):** em uma única linha, escape as aspas duplas ou use aspas simples no shell ao exportar. Em alguns .env não é possível quebra de linha; nesse caso use o JSON em uma linha só (o Google gera com quebras; você pode colar em uma linha removendo os `\n` ou usar Base64 – a API não faz decode de Base64, só lê o JSON direto).

### Opção alternativa: caminho do arquivo

```bash
export GCS_ENABLED=true
export GCS_BUCKET_NAME=vendalume-uploads-prod
export GCP_PROJECT_ID=vendalume-prod-123456
export GCS_CREDENTIALS_PATH=/etc/vendalume/gcs-key.json
# ou: export GOOGLE_APPLICATION_CREDENTIALS=/etc/vendalume/gcs-key.json
```

O `application.yml` já está preparado para as duas opções (`credentials-json` e `credentials-path`).

---

## 4. Uso na API

### Endpoint de upload

- **POST** `/api/upload`
- **Content-Type:** `multipart/form-data`
- **Corpo:** campo `file` (imagem) e, opcionalmente, `folder` (pasta lógica no bucket).
- **Autenticação:** JWT obrigatório.

Exemplo com `curl`:

```bash
curl -X POST "https://sua-api/api/upload?folder=tenants" \
  -H "Authorization: Bearer SEU_JWT" \
  -F "file=@/caminho/logo.png"
```

Resposta de sucesso (200):

```json
{ "url": "https://storage.googleapis.com/vendalume-uploads-prod/tenants/uuid.png" }
```

Essa **URL** deve ser usada onde a API espera uma URL de imagem:

- **Logo da empresa:** no corpo do PUT/PATCH da empresa (ex.: `logoUrl` no `TenantRequest`).
- **Foto de produto:** no corpo do create/update de produto (ex.: `imageUrl` ou `imageUrls`).

### Pastas sugeridas (`folder`)

| Valor de `folder` | Uso |
|------------------|-----|
| `tenants` | Logo das empresas. |
| `products` | Imagem principal e adicionais de produtos. |
| `uploads` | Padrão; uso genérico. |

### Regras do upload

- **Tipos permitidos:** JPEG, PNG, GIF, WebP.
- **Tamanho máximo:** 5 MB por arquivo.
- Foto de produto e logo da empresa são **opcionais**; o envio do arquivo e o preenchimento da URL nos DTOs ficam a critério do cliente (frontend/app).

---

## 5. Resumo rápido

1. Criar projeto no Google Cloud e ativar Cloud Storage API.  
2. Criar bucket (nome único, região adequada).  
3. (Opcional) Deixar o bucket público para leitura.  
4. Criar conta de serviço, baixar JSON da chave e dar permissão de “Desenvolvedor de objetos” no bucket.  
5. Definir `GCS_ENABLED=true`, `GCS_BUCKET_NAME`, `GCP_PROJECT_ID` e `GCS_CREDENTIALS_PATH` (ou `GOOGLE_APPLICATION_CREDENTIALS`).  
6. Usar **POST** `/api/upload` com `file` e opcionalmente `folder`; usar a `url` retornada em `logoUrl` (empresa) e `imageUrl`/`imageUrls` (produto).

Se `GCS_ENABLED` for `false` ou o bucket não estiver configurado, o **POST** `/api/upload` retornará **503** (Upload não configurado).
