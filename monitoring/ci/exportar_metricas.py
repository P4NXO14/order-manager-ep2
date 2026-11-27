import sys
import xml.etree.ElementTree as ET
from pathlib import Path

reporte = ET.parse(sys.argv[1]).getroot()
archivo = Path(sys.argv[2])

metricas = {
    "INSTRUCTION": "ci_coverage_instructions_percent",
    "BRANCH": "ci_coverage_branches_percent",
    "LINE": "ci_coverage_lines_percent"
}

resultados = []

for tipo, nombre in metricas.items():
    contador = reporte.find(f"./counter[@type='{tipo}']")

    if contador is None:
        raise ValueError(f"No existe el contador {tipo}")

    cubiertas = int(contador.attrib["covered"])
    no_cubiertas = int(contador.attrib["missed"])
    total = cubiertas + no_cubiertas
    porcentaje = cubiertas / total * 100 if total else 0

    resultados.append(f"# TYPE {nombre} gauge")
    resultados.append(f"{nombre} {porcentaje:.2f}")

    print(f"{nombre}: {porcentaje:.2f}%")

archivo.parent.mkdir(parents=True, exist_ok=True)
archivo.write_text("\n".join(resultados) + "\n", encoding="utf-8")