# Seguridad de FireFlow

FireFlow gestiona datos de clientes, grupos de presión y revisiones de campo.
Este documento describe cómo se protege el sistema y qué esperar si detectas
una vulnerabilidad.

## Versiones soportadas

| Versión | Soporte |
|---------|---------|
| 1.x (rama `main`) | ✅ Activo |

Solo se corrigen vulnerabilidades en la versión más reciente.

## Reportar una vulnerabilidad

Usa [GitHub Security Advisories](https://github.com/DeepZixSoul/fireflow/security/advisories)
del repositorio para reportar de forma privada. No publiques exploits en issues
abiertos. Acusaremos recibo en un máximo de 72 horas y coordinaremos la
divulgación.

## Controles implementados

### Android

- **Datos en disco**: Room sobre SQLCipher; la passphrase es aleatoria (32 bytes)
  y vive en el Android Keystore, con migración automática desde la passphrase
  legacy `DB_PASSPHRASE`.
- **Sesión**: `EncryptedSharedPreferences` con caducidad de 30 días.
- **Sin credenciales embebidas**: no hay usuarios ni contraseñas en el APK; el
  primer administrador se crea en el dispositivo en la pantalla de primer arranque.
- **Contraseñas**: Argon2id (tCost 3, mCost 64 MB, parallelism 4) con sal aleatoria.
- **Red**: HTTPS obligatorio en release, *cleartext* solo en builds de depuración
  y *certificate pinning* por variante (pin-set definido en build mediante
  `SERVER_PIN_DOMAIN` / `SERVER_PIN_SHA256`).
- **Capturas de pantalla**: `FLAG_SECURE` activo por defecto (desactivable en
  Ajustes → Seguridad).
- **Dispositivos rooteados**: aviso no bloqueante al arrancar; no se bloquea el
  uso de la app.
- **Registro**: logging de red sin cabecera `Authorization`; sin PII en los logs.

### Servidor (Ktor + PostgreSQL)

- **Contraseñas**: BCrypt cost 12; política de 8+ caracteres con mayúscula,
  minúscula y número (idéntica a la del cliente).
- **Sesión**: JWT HS256 con expiración de 15 minutos; el token deja de validarse
  si cambia la contraseña o el usuario se desactiva.
- **Cambio de contraseña**: `POST /api/v1/auth/change-password` autenticado, con
  rate limiting (5 req/min por IP) y revocación de tokens anteriores.
- **Bootstrap**: el primer administrador nace de `ADMIN_INITIAL_USERNAME` /
  `ADMIN_INITIAL_PASSWORD` (variables de entorno); no existe ningún usuario por
  defecto en las migraciones.
- **CORS**: lista exacta de orígenes vía `ALLOWED_ORIGINS`; en producción
  (`ENVIRONMENT=production`) el fallo de configuración bloquea el arranque.
- **Autenticación**: bloqueo tras 5 intentos fallidos (ventana de 15 minutos) y
  rate limiting en los endpoints de autenticación.
- **Inyección**: queries siempre parametrizadas (Exposed).
- **TLS**: `sslConnector` nativo cuando se configuran `SSL_KEYSTORE`,
  `SSL_KEY_ALIAS`, `SSL_KEYSTORE_PASSWORD` y `SSL_PRIVATE_KEY_PASSWORD`.
- **Logs**: sin contraseñas, tokens ni nombres de usuario.
- **Docker**: imagen multi-stage, usuario no root, sin puerto 5432 expuesto,
  healthcheck y limites de recursos.

### Build y CI

- Tests automatizados en cada push (Android + servidor).
- Escaneo OWASP **dependency-check** en CI (`security-scan`): falla el pipeline
  con CVEs de CVSS ≥ 7 e informe HTML/JSON como artefacto. Puede acelerarse con
  el secreto `NVD_API_KEY`.
- `local.properties`, `.env` y los informes de auditoría internos no se versionan.
- Contraseñas y secretos nunca se guardan en el repositorio; rota cualquier
  credencial que haya quedado en el historial de git antes de un despliegue real.

## Limitaciones conocidas

- La auditoría interna (`SECURITY_AUDIT.md`) no se distribuye con el repo.
- Si la passphrase legacy `DB_PASSPHRASE` cambia en un equipo con datos, la
  migración de rekey falla a propósito y hay que restablecer la contraseña de
  la BD.
- Sin `SERVER_PIN_*` en la variante release, el cliente exige TLS pero no fija
  pins (se recomienda definirlos para producción).
- El aviso de dispositivo rooteado es informativo: no impide el uso de la app.

## Recomendaciones de despliegue

1. `ENVIRONMENT=production` y `ALLOWED_ORIGINS` con el dominio real.
2. `JWT_SECRET` de al menos 32 caracteres y rotado respecto al de desarrollo.
3. Keystore con TLS (`SSL_*`) y `sslmode=require` en la conexión a PostgreSQL.
4. `ADMIN_INITIAL_PASSWORD` con la política completa (8+, mayúscula, minúscula,
   número); rotar tras el primer inicio de sesión.
5. Definir el pin-set de release (`SERVER_PIN_*`) y el secreto `NVD_API_KEY` en CI.
