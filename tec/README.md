# Comanda Falada · Demonstração Tecnológica Base

Este diretório contém a implementação demonstrável do **Comanda Falada · Passo à Frente**, integrando as 4 peças abertas da arquitetura:
1. **ASR (faster-whisper / whisper.cpp):** Transcrição de áudios em português brasileiro com injeção de vocabulário de cardápio via `initial_prompt`.
2. **GraphRAG (NetworkX):** Grafo de conhecimento com bares, bairros do Recife, níveis de conforto acústico (decibéis e tipo de som), relatos de acessibilidade física e cardápios estruturados (`bares_grafo.json`).
3. **Regras Determinísticas (`regras.py`):** Soma exata da conta sem erro de arredondamento/ponto flutuante, validação de teto de gasto, cálculo de proximidade GPS via fórmula de Haversine, geração de senha falada de 4 dígitos e payload Pix Copia e Cola (BR Code EMV) com CRC16 válido.
4. **Aplicação Android Nativa e APK Demonstrativo:** Código Java nativo em `android_app/` e APK assinado `comanda-falada.apk` pronto para instalação e teste no celular.

## Estrutura de Arquivos

- `transcrever.py`: Pipeline principal integrando transcrição, consulta ao grafo de bares e cálculo da comanda.
- `grafo.py`: Módulo NetworkX para modelagem e recuperação em grafo.
- `regras.py`: Módulo determinístico para comanda, Haversine, senha e Pix.
- `bares_grafo.json`: Base de conhecimento de bares do Recife e cardápios.
- `audios/`: Áudios em português para teste dos 3 casos (incluindo áudio com ruído simulado de bar).
- `comanda-falada.apk`: Aplicativo demonstrativo compilado para dispositivos Android.
- `android_app/`: Código-fonte da aplicação Android (layout acessível e Text-to-Speech).
- `compilar_apk.ps1`: Script PowerShell autônomo de compilação do APK usando o Android SDK.

## Como rodar o pipeline no PC

```bash
pip install -r requirements.txt
python transcrever.py
```

## Como instalar o APK no Android

Conecte o smartphone Android com a Depuração USB ativada e execute:

```bash
adb install -r comanda-falada.apk
```
Ou copie o arquivo `comanda-falada.apk` para o armazenamento do celular e toque nele para instalar diretamente.
