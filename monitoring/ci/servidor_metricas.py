import os
import subprocess
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from urllib.parse import urlsplit

if os.environ.get("GITHUB_METRICS_TOKEN"):
    subprocess.Popen([
        sys.executable,
        "/metrics/actualizar_metricas.py"
    ])

class ServidorMetricas(BaseHTTPRequestHandler):
    def do_GET(self):
        if urlsplit(self.path).path != "/metrics":
            self.send_error(404)
            return

        try:
            contenido = Path("/metrics/metricas.prom").read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
            self.end_headers()
            self.wfile.write(contenido)
        except OSError:
            self.send_error(503, "Metricas no disponibles")

HTTPServer(("0.0.0.0", 9101), ServidorMetricas).serve_forever()