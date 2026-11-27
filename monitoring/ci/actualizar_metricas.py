import io
import json
import os
import time
import zipfile
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from urllib.parse import urlsplit

REPOSITORIO = "P4NXO14/order-manager-ep2"
RAMA = "evaluacion-3"
WORKFLOW = "ci-cd.yml"
ARTEFACTO = "metricas-pipeline"
INTERVALO = 60

TOKEN = os.environ.get("GITHUB_METRICS_TOKEN", "").strip()
API = f"https://api.github.com/repos/{REPOSITORIO}"
ARCHIVO = Path(__file__).parent / "metricas.prom"

ultimo_artefacto = None

def crear_solicitud(url):
    cabeceras = {
        "Accept": "application/vnd.github+json",
        "User-Agent": "OrderManager-Metrics",
        "X-GitHub-Api-Version": "2022-11-28"
    }

    solicitud = Request(url, headers=cabeceras)

    if TOKEN:
        solicitud.add_unredirected_header(
            "Authorization", f"Bearer {TOKEN}"
        )

    return solicitud

def consultar(url):
    solicitud = crear_solicitud(url)

    with urlopen(solicitud, timeout=30) as respuesta:
        return json.loads(respuesta.read())

def descargar(artefacto_id):
    url = f"{API}/actions/artifacts/{artefacto_id}/zip"
    solicitud = crear_solicitud(url)

    with urlopen(solicitud, timeout=30) as respuesta:
        datos = respuesta.read()

    with zipfile.ZipFile(io.BytesIO(datos)) as archivo_zip:
        return archivo_zip.read("metricas.prom")

def actualizar():
    global ultimo_artefacto

    url = (
        f"{API}/actions/workflows/{WORKFLOW}/runs"
        f"?branch={RAMA}&status=success&per_page=10"
    )

    ejecuciones = consultar(url)["workflow_runs"]

    for ejecucion in ejecuciones:
        ejecucion_id = ejecucion["id"]

        artefactos = consultar(
            f"{API}/actions/runs/{ejecucion_id}/artifacts"
        )["artifacts"]

        for artefacto in artefactos:
            if artefacto["name"] != ARTEFACTO or artefacto["expired"]:
                continue

            if artefacto["id"] == ultimo_artefacto:
                return

            contenido = descargar(artefacto["id"])

            requeridas = [
                b"ci_coverage_instructions_percent ",
                b"ci_coverage_branches_percent ",
                b"ci_coverage_lines_percent ",
                b"ci_deployment_duration_seconds "
            ]

            if not all(metrica in contenido for metrica in requeridas):
                raise ValueError("El artefacto no contiene todas las metricas")

            temporal = ARCHIVO.with_suffix(".tmp")
            temporal.write_bytes(contenido)
            temporal.replace(ARCHIVO)

            ultimo_artefacto = artefacto["id"]

            print(
                f"Metricas actualizadas desde ejecucion {ejecucion_id}",
                flush=True
            )

            return

    print("No se encontraron artefactos disponibles", flush=True)

if not TOKEN:
    raise SystemExit("GITHUB_METRICS_TOKEN no configurado")

while True:
    try:
        actualizar()
    except HTTPError as error:
        servidor = urlsplit(error.url).hostname
        print(
            f"Error HTTP {error.code} en {servidor}",
            flush=True
        )
    except (URLError, OSError, ValueError, KeyError,
            zipfile.BadZipFile) as error:
        print(
            f"Error actualizando metricas: {type(error).__name__}",
            flush=True
        )

    time.sleep(INTERVALO)