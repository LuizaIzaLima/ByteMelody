# Ninho Sonoro

App Jetpack Compose para sono do bebe, com visual claro, macio e acolhedor.

## O que tem

- Tela unica feita em Compose + Material 3.
- Botao central de play/pause com anel de progresso do timer.
- Sons gerados em tempo real com `AudioTrack`: utero calmo, chuva fina, ruido branco, vento leve, caixinha e coracao.
- Controle de volume.
- Temporizador de 15, 30, 45, 60 minutos ou modo livre.
- Icone adaptativo e identidade visual propria.

## Rodar

Abra esta pasta no Android Studio:

`outputs/NinhoSonoro`

Ou compile pelo terminal:

```powershell
.\gradlew.bat :app:assembleDebug
```

## GitHub

Este projeto ja esta pronto para subir no GitHub:

- Tem `.gitignore`.
- Nao depende de arquivos locais versionados.
- Inclui Gradle Wrapper.
- O Android Studio recria `local.properties` automaticamente.

Veja o passo a passo em `COMO_SUBIR_NO_GITHUB.md`.
