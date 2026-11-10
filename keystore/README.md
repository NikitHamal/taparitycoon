# Public signing keystore

This keystore is intentionally committed. It signs every release APK so new builds install over old ones.
Google Play re-signs with its own key in production, so it does not need to be secret.

- file: `tapari-release.p12` (PKCS12)
- store password: `tapari123`
- key alias: `tapari`
- key password: `tapari123`
