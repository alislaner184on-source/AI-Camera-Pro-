# AI Camera Pro 0.1 (Moto G24)

Projeto inicial Android em Kotlin com CameraX. **Não contém IA ainda** e não é um APK compilado.

## Recursos implementados
- Pré-visualização de câmera, fotos JPEG e vídeos MP4 em qualidade HD (720p, com fallback).
- Câmera frontal/traseira, zoom por botões, gravação de áudio quando autorizada.
- Salvamento em Pictures/AI Camera Pro e Movies/AI Camera Pro via MediaStore.

## Como compilar
1. Instale Android Studio em um computador e abra a pasta `AI_Camera_Pro` extraída.
2. Configure Android SDK 35 e JDK 17. Aguarde a sincronização do Gradle.
3. Conecte o Moto G24 com depuração USB ativada ou gere APK via Build > Build APK(s).
4. Instale o APK gerado em `app/build/outputs/apk/debug/app-debug.apk`.
5. Conceda acesso à câmera e, se desejar, ao microfone.

**Atenção:** o projeto ainda não foi compilado/testado em dispositivo real neste ambiente. Como não há Gradle Wrapper no ZIP, o Android Studio deve usar uma instalação de Gradle compatível (8.9) ou você deve gerar o wrapper localmente. Não execute scripts de fontes desconhecidas.

## Próximas etapas
- Diagnóstico Camera2 (RAW, MANUAL_SENSOR, resoluções e FPS realmente suportados).
- Seleção de resolução e taxa de quadros, foco e exposição.
- ImageAnalysis com orçamento de latência e modelo TFLite quantizado.
- Pós-processamento de foto com redução de ruído e HDR multi-frame, medindo qualidade e aquecimento.
- Pipeline de vídeo com GPU (OpenGL/MediaCodec) e fallback sem IA.

## Compilação na nuvem sem PC
O workflow `.github/workflows/build-apk.yml` compila um APK debug via GitHub Actions.
Crie um repositório no GitHub e envie **os arquivos e pastas do projeto** para a raiz (não envie apenas o ZIP). Pelo navegador móvel, a interface do GitHub pode dificultar o envio de pastas: use um editor Git compatível com Android ou a interface web em modo desktop, conforme disponível. Depois acesse Actions > Compilar APK Android > Run workflow; ao concluir, baixe o artifact AI-Camera-Pro-debug-APK, extraia seu ZIP e instale o app-debug.apk.
O workflow não foi executado neste ambiente. A compilação poderá revelar erros que precisem de correção. Instale somente APKs gerados no seu próprio repositório.
