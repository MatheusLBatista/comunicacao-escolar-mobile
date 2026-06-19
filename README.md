# comunicacao-escolar-mobile

App Android do sistema de comunicação escolar. Kotlin + Jetpack Compose.

## Requisitos

- Android Studio Hedgehog ou superior
- JDK 11
- API mínima: 28 (Android 9)
- API da backend rodando localmente (ver [comunicacao-escolar-api](../comunicacao-escolar-api))

## Configuração

Clone e abra o projeto no Android Studio. O emulador acessa a API local via `http://10.0.2.2:5000/`.

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

## Endpoints relevantes

- Base (emulador): `http://10.0.2.2:5000/`
- Base (dispositivo físico): IP da máquina na rede local, porta 5000
- Auth: `POST /auth/login`
- Mensagens: `POST /conversations/{conversationId}/messages`
  - Header: `Authorization: Bearer {token}`
  - Body: `{ "text": "conteudo" }`
