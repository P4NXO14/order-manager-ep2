import json
import subprocess
import sys

SERVICIOS = {
    "db", "app", "prometheus", "grafana", "loki", "alloy", "ci-metrics"
}

PUERTOS = {
    "prometheus": 9090,
    "grafana": 3000,
    "loki": 3100,
    "alloy": 12345,
    "ci-metrics": 9101
}

def auditar(configuracion):
    servicios = configuracion.get("services", {})
    resultados = []

    def verificar(nombre, cumple):
        resultados.append(bool(cumple))
        print(f"[{'OK' if cumple else 'FAIL'}] {nombre}")

    verificar("Siete servicios requeridos",
            SERVICIOS <= servicios.keys())

    verificar("Sin contenedores privilegiados", all(
        not s.get("privileged", False)
        for s in servicios.values()
    ))

    verificar("Sin red de tipo host", all(
        s.get("network_mode") != "host"
        for s in servicios.values()
    ))

    for nombre, puerto in PUERTOS.items():
        puertos = servicios.get(nombre, {}).get("ports", [])

        verificar(f"Puerto local de {nombre}", bool(puertos) and all(
            p.get("host_ip") == "127.0.0.1"
            for p in puertos
        ) and any(
            p.get("target") == puerto
            and str(p.get("published")) == str(puerto)
            for p in puertos
        ))

    verificar("PostgreSQL sin puertos publicados",
            "db" in servicios and not servicios["db"].get("ports"))

    for nombre in ("app", "db"):
        health = servicios.get(nombre, {}).get("healthcheck", {})
        test = health.get("test", [])

        verificar(f"Healthcheck de {nombre}",
                bool(test)
                and not health.get("disable", False)
                and test != ["NONE"])

    sockets = [
        (nombre, v)
        for nombre, s in servicios.items()
        for v in s.get("volumes", [])
        if isinstance(v, dict)
        and (
            v.get("source") == "/var/run/docker.sock"
            or v.get("target") == "/var/run/docker.sock"
        )
    ]

    verificar("Socket Docker exclusivo de Alloy y en solo lectura",
            len(sockets) == 1
            and sockets[0][0] == "alloy"
            and sockets[0][1].get("read_only") is True)

    print("AUDITORIA APROBADA" if all(resultados)
        else "AUDITORIA RECHAZADA")

    return 0 if all(resultados) else 1

if __name__ == "__main__":
    try:
        if "--stdin" in sys.argv:
            configuracion = json.load(sys.stdin)
        else:
            proceso = subprocess.run(
                ["docker", "compose", "--env-file", ".env",
                "config", "--format", "json"],
                capture_output=True,
                text=True,
                check=True
            )

            configuracion = json.loads(proceso.stdout)

        sys.exit(auditar(configuracion))

    except (OSError, ValueError, subprocess.CalledProcessError):
        print("[FAIL] No se pudo leer la configuracion de Docker Compose")
        sys.exit(1)