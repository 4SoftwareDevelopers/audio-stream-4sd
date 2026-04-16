Feature: El administrador puede consultar los mensajes de voz pendientes de reproducción con base en filtros como la fecha,
el stream_id, username, etc.

Goal:
Crear un endpoint que me permita obtener los mensajes de voz pendientes de reproducción con base en filtros como la fecha,
el stream_id, username, etc.

Constraints:
- La información debe ser acorde a los filtros que se envíen a través del endpoint.
- La información siempre debe venir paginada, nunca consultar toda la tabla.
- Los filtros se deben aplicar directamente en SQL, no a través de listas.