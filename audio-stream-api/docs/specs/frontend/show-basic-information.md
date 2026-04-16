Feature: Poder mostrar en el frontend de los usuarios, si el streamer está en vivo, quienes son los miembros del canal y próximos streams.

Goal:
Construir un endpoint que permita al frontend de los usuarios saber si el streamer está en vivo.

Constraints:
- Usar el API de YouTube Data API v3.
- Consultar la documentación disponible https://developers.google.com/youtube/v3/live/docs/liveBroadcasts/list.
- Este endpoint debe responder info básica del canal, como el logo, el nombre, el estado del stream y el título del stream.
- Consultar los próximos live streams usando el api de YouTube Data API v3.
- Consultar los miembros del canal usando el api de YouTube Data API v3.
- En la respuesta incluir un disclaimer con restricciones de uso.
