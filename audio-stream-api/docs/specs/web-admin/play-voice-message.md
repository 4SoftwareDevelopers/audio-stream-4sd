Feature: Permitir el administrador reproducir un mensaje de voz a través de OBS.

Goal:
Construir un endpoint para lograr enviar un mensaje de voz para que OBS lo reproduzca, para esto se usarán websockets.

Constraints:
- Se debe construir un simple web socket para poder enviar el audio y que el cliente que escucha este socket pueda reproducir el audio.