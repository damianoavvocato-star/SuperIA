# Android signing and updates

SuperIA uses one persistent Android signing key for every installable APK.

## Required GitHub Actions repository secrets

Configure these secrets in **Settings → Secrets and variables → Actions → New repository secret**:

- `SUPERIA_KEYSTORE_BASE64`: the complete single-line Base64 content of the private keystore file.
- `SUPERIA_KEYSTORE_PASSWORD`: keystore password.
- `SUPERIA_KEY_ALIAS`: private-key alias.
- `SUPERIA_KEY_PASSWORD`: private-key password.

The repository is public. Never commit the keystore, passwords, Base64 content, or a keystore artifact to the repository. GitHub Actions decodes the key only into the runner's temporary directory and does not upload it.

## Build behavior

- Unit and emulator tests use the normal debug variant.
- The installable release APK is built and uploaded only when the persistent signing secrets are present.
- The application ID remains `it.superiaprivata.app`.
- CI sets a monotonically increasing version code from the GitHub Actions workflow run number.
- Every future installable APK must use the same keystore and alias. Losing or replacing this key prevents in-place updates.

## First clean installation

Install the first APK produced with the persistent key after confirming the signing secrets are configured and the release build succeeds. Future APKs from the same workflow can then update that installation without uninstalling it.

The old debug-signed installation may reject the first persistent-key APK because Android requires matching signatures for in-place updates. If that happens, uninstall the old app only after confirming there is no data to preserve. The local Gemma model must be imported again from its original file.

Keep an offline backup of the original keystore and its passwords in a secure location separate from GitHub.
