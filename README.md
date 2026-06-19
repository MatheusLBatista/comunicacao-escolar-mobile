# Comunicação Escolar — Mobile

App Android do sistema de comunicação escolar. Centraliza a troca de informações entre instituições de ensino, professores e responsáveis, cobrindo desde comunicados diários e mural de avisos até chat em tempo real e controle de saída.

## Tecnologias

<img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" /> <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" /> <img alt="Retrofit" src="https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white" /> <img alt="Socket.IO" src="https://img.shields.io/badge/Socket.IO-010101?style=for-the-badge&logo=socketdotio&logoColor=white" /> <img alt="Firebase" src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" /> <img alt="Coil" src="https://img.shields.io/badge/Coil-000000?style=for-the-badge&logo=android&logoColor=white" />

## Features

- **Autenticação** com JWT (access token + refresh token automático), recuperação de senha e cadastro por convite.
- **Dashboard por papel** — fluxos distintos para Administrador, Professor e Responsável.
- **Comunicados diários** — professores registram a rotina dos alunos; responsáveis visualizam e confirmam leitura.
- **Mural de avisos** — publicação e visualização de posts da escola ou turma, com suporte a curtidas.
- **Chat em tempo real** via Socket.IO — mensagens privadas entre professores e responsáveis.
- **Agenda escolar** — visualização de eventos por escola ou turma.
- **Controle de saída** — cadastro e consulta de autorizados para retirada de alunos.
- **Notificações push** via Firebase Cloud Messaging para novos comunicados, mensagens e avisos.
- **Imagens e anexos** carregados via MinIO com Coil.
- **Tema claro/escuro** configurável pelo usuário.

## Pré-requisitos

- [Android Studio](https://developer.android.com/studio) Hedgehog ou superior
- JDK 11
- API mínima: 28 (Android 9) / Target: 36
- [comunicacao-escolar-api](../comunicacao-escolar-api) rodando localmente

## Instalação e configuração

**1. Clone o repositório**

```bash
git clone https://gitlab.fslab.dev/fabrica-de-software-iv/comunicacao-escolar/comunicacao-escolar-mobile.git
```

Abra o projeto no Android Studio e aguarde a sincronização do Gradle.

**2. Configure o endereço da API**

Edite `BASE_URL` em `app/src/main/java/.../network/RetrofitClient.kt`:

- **Emulador:** mantenha `http://localhost:3011/` e use `adb reverse` (ver abaixo).
- **Dispositivo físico:** troque por `http://<IP-da-máquina>:3011/` e garanta que `MINIO_PUBLIC_URL` na API use o mesmo IP.

**3. Configure o adb reverse (obrigatório no emulador)**

O emulador Android não acessa `localhost` da máquina host diretamente. Execute após iniciar o emulador e a API:

```bash
adb reverse tcp:3011 tcp:3011   # API
adb reverse tcp:9000 tcp:9000   # MinIO (imagens e arquivos)
```

Sem o reverse da porta 9000, imagens de perfil, fotos de autorização e outros anexos não carregam mesmo que o login e os dados textuais funcionem normalmente.

**4. Configure o Firebase (notificações push)**

O arquivo `app/google-services.json` está no `.gitignore` e não está no repositório. Para notificações push funcionarem, baixe-o do [Firebase Console](https://console.firebase.google.com) (Configurações do projeto → Seus apps → Android) e coloque em `app/google-services.json`. Sem esse arquivo o build compila, mas notificações push não funcionam.

## Executando

Execute via Android Studio (botão **Run**) ou pela linha de comando:

```bash
# Build e instalar no dispositivo/emulador conectado (debug)
./gradlew installDebug

# Build APK de release
./gradlew assembleRelease
```

### Login rápido (modo dev)

Em builds de debug, um botão **DEV** aparece no rodapé da tela de login. Ao clicar, abre um seletor com os usuários padrão da seed:

| Papel        | E-mail                | Senha       |
| :----------- | :-------------------- | :---------- |
| Admin        | `admin@admin.com`     | `Senha@123` |
| Professor    | `teacher@teacher.com` | `Senha@123` |
| Responsável  | `parent@parent.com`   | `Senha@123` |

Para desativar o botão sem trocar de build type, edite `app/build.gradle.kts`:

```kotlin
debug {
    buildConfigField("boolean", "DEV_LOGIN_ENABLED", "false") // ← trocar aqui
}
```

O botão nunca aparece em builds de release (`DEV_LOGIN_ENABLED = false` fixo).

## Estrutura do projeto

```
app/src/main/java/dev/fslab/comunicacao/escolar/
├── model/           # Data classes (requests, responses, modelos de UI)
├── navigation/      # NavGraph e extensões de navegação
├── network/         # Retrofit, interceptors, TokenManager, Socket.IO, Firebase
├── ui/
│   ├── components/  # Componentes reutilizáveis (AppHeader, BottomNavBar…)
│   ├── screens/     # Telas agrupadas por papel
│   │   ├── admin/
│   │   ├── auth/
│   │   ├── common/
│   │   ├── conversas/
│   │   ├── mural/
│   │   ├── professor/
│   │   └── responsavel/
│   ├── theme/       # Cores, tipografia e tema Compose
│   └── viewmodel/   # ViewModels por funcionalidade
└── util/            # Utilitários (DateUtils…)
```

## Equipe

| Nome            | Papel         | Contato                     |
| :-------------- | :------------ | :-------------------------- |
| Arthur Gomes    | Desenvolvedor | piclekrick@gmail.com        |
| Matheus Batista | Desenvolvedor | matheusifro2020@gmail.com   |
| Silvio Ribeiro  | Desenvolvedor | silviohuan@gmail.com        |
| Vinícius Moraes | Desenvolvedor | viniciusmoraesvha@gmail.com |

**Cliente:** Gilberto Pereira da Silva — gilberto.silva@ifro.edu.br

## Licença

Distribuído sob a licença [MIT](https://opensource.org/licenses/MIT).
