# comunicacao-escolar-mobile

App Android do sistema de comunicação escolar. Kotlin + Jetpack Compose.

## Requisitos

- Android Studio Hedgehog ou superior
- JDK 11
- API mínima: 28 (Android 9)
- API da backend rodando localmente (ver [comunicacao-escolar-api](../comunicacao-escolar-api))

## Configuração

Clone e abra o projeto no Android Studio.

A URL da API fica em `RetrofitClient.kt` (`BASE_URL`). O app usa `http://localhost:3011/` — que funciona no emulador **somente** com `adb reverse` ativo (ver abaixo).

### adb reverse (obrigatório no emulador)

O emulador Android não acessa `localhost` da máquina host sem redirecionamento. Execute após iniciar o emulador e a API:

```bash
adb reverse tcp:3011 tcp:3011   # API
adb reverse tcp:9000 tcp:9000   # MinIO (imagens e arquivos)
```

Sem o reverse da porta 9000, imagens de perfil, fotos de autorização e outros anexos não carregam mesmo que o login e os dados textuais funcionem normalmente.

> **Dispositivo físico:** troque `BASE_URL` pelo IP da máquina na rede local (ex.: `http://192.168.1.10:3011/`) e garanta que `MINIO_PUBLIC_URL` na API também use esse IP.

## Desenvolvimento

### Login rápido (modo dev)

Em builds de debug, um botão **DEV** aparece no rodapé da tela de login. Ao clicar, abre um seletor com os usuários padrão da seed:

| Papel        | E-mail                  | Senha      |
|--------------|-------------------------|------------|
| Admin        | `admin@admin.com`       | `Senha@123` |
| Professor    | `teacher@teacher.com`   | `Senha@123` |
| Responsável  | `parent@parent.com`     | `Senha@123` |

Para desativar o botão sem trocar de build type, edite `app/build.gradle.kts`:

```kotlin
debug {
    buildConfigField("boolean", "DEV_LOGIN_ENABLED", "false") // ← trocar aqui
}
```

O botão nunca aparece em builds de release (`DEV_LOGIN_ENABLED = false` fixo).

