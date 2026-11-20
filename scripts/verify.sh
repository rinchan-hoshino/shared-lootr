#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_NAME="${SHARED_LOOTR_VERIFY_IMAGE:-shared-lootr-verify:latest}"
USE_CONTAINER=0

for arg in "$@"; do
  case "${arg}" in
    --container)
      USE_CONTAINER=1
      ;;
    *)
      echo "Unknown option: ${arg}" >&2
      echo "Usage: $0 [--container]" >&2
      exit 2
      ;;
  esac
done

detect_jdk() {
  local version="$1"
  local var_name="JAVA_${version}_HOME"
  local existing="${!var_name:-}"
  if [[ -n "${existing}" && -d "${existing}" ]]; then
    echo "${existing}"
    return 0
  fi
  if [[ -x "/usr/libexec/java_home" ]]; then
    local mac_home
    mac_home="$(/usr/libexec/java_home -v "${version}" 2>/dev/null || true)"
    if [[ -n "${mac_home}" && -d "${mac_home}" ]]; then
      echo "${mac_home}"
      return 0
    fi
  fi
  for candidate in \
    "/opt/jdks/temurin-${version}" \
    /usr/lib/jvm/temurin-"${version}"-jdk-* \
    /usr/lib/jvm/java-"${version}"-*; do
    if [[ -d "${candidate}" && -x "${candidate}/bin/java" ]]; then
      echo "${candidate}"
      return 0
    fi
  done
  return 1
}

if [[ "${USE_CONTAINER}" -eq 0 ]]; then
  J17="$(detect_jdk 17 || true)"
  J21="$(detect_jdk 21 || true)"
  J25="$(detect_jdk 25 || true)"
  if [[ -n "${J17}" && -n "${J21}" && -n "${J25}" ]]; then
    export JAVA_17_HOME="${J17}"
    export JAVA_21_HOME="${J21}"
    export JAVA_25_HOME="${J25}"
    echo "==> Running Shared Lootr full 3-gate verification natively (JDK 17/21/25 detected)"
    cd "${REPO_ROOT}"
    python3 scripts/build-matrix.py
    exit 0
  else
    echo "==> Host is missing one or more required JDKs (17=${J17:-missing}, 21=${J21:-missing}, 25=${J25:-missing}); falling back to containerized verification."
    USE_CONTAINER=1
  fi
fi

if command -v docker >/dev/null 2>&1; then
  ENGINE="docker"
elif command -v podman >/dev/null 2>&1; then
  ENGINE="podman"
else
  echo "Error: neither docker nor podman is available, and host lacks JDK 17/21/25." >&2
  exit 1
fi

echo "==> Building verification container image (${IMAGE_NAME}) via ${ENGINE}..."
"${ENGINE}" build -f "${REPO_ROOT}/Dockerfile.verify" -t "${IMAGE_NAME}" "${REPO_ROOT}"

echo "==> Running Shared Lootr full 3-gate verification inside container..."
exec "${ENGINE}" run --rm --init \
  -e ORG_GRADLE_PROJECT_neoform_decompile_maxMemory=2g \
  -e GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx2560m" \
  -v "${REPO_ROOT}:/workspace" \
  -v rinchan-gradle-cache-shared-lootr:/root/.gradle \
  -w /workspace \
  "${IMAGE_NAME}" \
  python3 scripts/build-matrix.py
