Feature: Permitir que el administrador pueda gestionar los registros de access_filter.

Goal:
Construir los endpoint para poder gestionar (CRUD) la tabla access_filter con diferentes filtros relacionados a los campos dicha tabla.

Constraints:
- La información siempre debe venir paginada, nunca consultar toda la tabla.
- Los filtros se deben aplicar directamente en SQL, no a través de listas.
- No se puede filtrar por el campo id o la primary key.