Feature: Permitir el administrador iniciar sesión en el sistema.

Goal:
Construir un endpoint para que el administrador debe poder autenticarse en el sistema y también recuperar su contraseña en caso de ser necesario.

Constraints:
- Utilizar JWT sin OAuth, sin embargo el token debe ser seguro y debe estar firmado.
- El link para recuperación de contraseña debe tener un tiempo de vida máximo a 5 minutos.
- La nueva contraseña debe cumplir con las características mínimas de un password seguro.