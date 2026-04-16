Feature: El administrador puede marcar un mensaje de voz como "black list".

Goal:
Construir un endpoint que permita marcar como "black list" un mensaje de voz y así llevar un control de los mensajes que no deben ser reproducidos por el streamer y almacenar la ip asociada a esos mensajes de voz.

Constraints:
- Marcar un mensaje de voz como "black list", implica crear un registro en la tabla access_filter con estado status "BLACKLIST".
- No se permite que una misma ip envié más de 3 audios por stream, si llega a ese límite se marca como "BLOCKED" en access_filter.
- Se debe validar que el audio no dure más de 1 minuto y 30 segundos.
- Se debe validar que el audio no supere los 1MB de tamaño.
- Implementar captcha de cloudflare.

