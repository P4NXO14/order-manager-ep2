# OrderManager - Evaluación Parcial 2 DevOps

**Estudiante:** Francisco Osorio  
**Asignatura:** Ingeniería DevOps 
**Evaluación:** Parcial 2  

## 1. Descripción del proyecto

OrderManager es un microservicio desarrollado con Java 17, Spring Boot y Maven, que permite gestionar pedidos de clientes mediante una API REST.

Para esta evaluación se utilizó como base el proyecto entregado por el profesor, al cual se le incorporaron herramientas de DevOps para automatizar las pruebas, revisar la calidad del código y realizar despliegues mediante Docker.

El objetivo principal fue implementar un pipeline CI/CD con GitHub Actions que permita comprobar el funcionamiento del proyecto antes de desplegarlo.

## 2. Tecnologías utilizadas

| Tecnología | Función |
|---|---|
| Java 17 y Spring Boot | Desarrollo del microservicio |
| Maven | Compilación y dependencias |
| JUnit 5, Mockito y MockMvc | Pruebas automatizadas |
| JaCoCo | Cobertura de pruebas |
| Docker | Contenerización |
| Docker Compose | Orquestación de contenedores |
| PostgreSQL | Persistencia de datos |
| H2 | Base de datos local |
| GitHub Actions | Pipeline CI/CD |
| Dependabot | Revisión de dependencias |
| SonarQube Cloud | Análisis de código y Quality Gate |

## 3. Funcionalidades

OrderManager permite realizar las siguientes operaciones:

- Crear y consultar pedidos.
- Agregar productos con sus cantidades y precios.
- Calcular automáticamente el total de los pedidos.
- Buscar pedidos por identificador o estado.
- Confirmar, enviar, entregar y cancelar pedidos.
- Validar los datos ingresados y controlar posibles errores.

Los estados disponibles son `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED` y `CANCELLED`.

## 4. Instalación y ejecución

Para ejecutar el proyecto se necesita tener instalado Java 17, Maven, Git y Docker Desktop.

Primero se debe clonar el repositorio:

```powershell
git clone https://github.com/P4NXO14/order-manager-ep2.git
cd order-manager-ep2
```

Para compilar y ejecutar las pruebas:

```powershell
mvn clean verify
```

Para iniciar la aplicación localmente:

```powershell
mvn spring-boot:run
```

El microservicio utiliza el puerto `8098`.

Su funcionamiento se puede comprobar desde:

http://localhost:8098/actuator/health

Si está funcionando correctamente, devuelve el estado `UP`.

## 5. Pruebas automatizadas y JaCoCo

El proyecto cuenta con 38 pruebas automatizadas, distribuidas entre las clases `OrderServiceTest`, `OrderControllerTest` y `OrderModelTest`.

Estas pruebas permiten comprobar las operaciones del microservicio, las validaciones y las respuestas de la API.

Durante las verificaciones se obtuvieron los siguientes resultados:

| Indicador | Resultado |
|---|---|
| Pruebas ejecutadas | 38 |
| Pruebas fallidas | 0 |
| Cobertura de instrucciones | 91 % |
| Cobertura de ramas | 98 % |
| Cobertura mínima configurada | 75 % |

JaCoCo está configurado para que Maven rechace la compilación si la cobertura de instrucciones es inferior al 75 %.

Para ejecutar las pruebas:

```powershell
mvn clean verify
```

Para consultar el reporte de cobertura:

```powershell
Start-Process ".\target\site\jacoco\index.html"
```

## 6. Docker y Docker Compose

Se implementó un Dockerfile multi-stage que utiliza Maven para compilar el proyecto y una imagen Java JRE para ejecutar la aplicación.

La imagen también utiliza un usuario sin privilegios de administrador.

Para construir la imagen:

```powershell
docker build -t order-manager:ep2 .
```

Para ejecutar OrderManager individualmente:

```powershell
docker run -d --name order-manager-ep2 -p 8098:8098 order-manager:ep2
```

Además, se configuró Docker Compose para ejecutar OrderManager junto con PostgreSQL.

Antes de iniciar los contenedores se debe crear un archivo `.env` en la carpeta principal:

```dotenv
POSTGRES_PASSWORD=COLOCA_UNA_CLAVE_SEGURA_AQUI
```

Se debe reemplazar el valor de ejemplo por una contraseña propia.

Para iniciar ambos servicios:

```powershell
docker compose up -d --build
```

Para comprobar los contenedores:

```powershell
docker compose ps
```

PostgreSQL utiliza un volumen para mantener los datos almacenados. Su funcionamiento se comprobó creando un pedido y verificando que continuara registrado después de reiniciar los contenedores.

También se configuraron healthchecks para verificar el estado de PostgreSQL y OrderManager.

Para detener los servicios sin eliminar los datos:

```powershell
docker compose down
```

## 7. Pipeline CI/CD

El pipeline se configuró mediante GitHub Actions en el archivo `.github/workflows/ci-cd.yml`.

Se ejecuta al realizar cambios en la rama `main`, mediante pull requests y también de forma manual.

El pipeline está dividido en dos trabajos.

### Pruebas y análisis de código

En este trabajo se realizan las siguientes tareas:

1. Descargar el código desde GitHub.
2. Configurar Java y Maven.
3. Ejecutar las 38 pruebas automatizadas.
4. Validar la cobertura mínima con JaCoCo.
5. Analizar el código mediante SonarQube.
6. Comprobar el Quality Gate.
7. Guardar los reportes de pruebas.

### Despliegue simulado

Si las comprobaciones anteriores son correctas, el pipeline continúa con:

1. Construcción de la imagen Docker.
2. Inicio de OrderManager y PostgreSQL.
3. Comprobación del estado de la aplicación.
4. Creación de un pedido de prueba.
5. Verificación de la persistencia de datos.

Si alguna comprobación obligatoria falla, el despliegue no se ejecuta.

El despliegue se realiza en un entorno temporal de GitHub Actions, no en un servidor público permanente.

Durante el desarrollo se obtuvo una ejecución exitosa del pipeline #16, con los dos trabajos aprobados.

**Ejecuciones del pipeline:**

https://github.com/P4NXO14/order-manager-ep2/actions

## 8. Seguridad y calidad

Se utilizaron distintas herramientas para mejorar la seguridad y controlar la calidad del proyecto.

**Dependabot:** revisa semanalmente las dependencias de Maven, Docker y GitHub Actions, proponiendo actualizaciones mediante pull requests.

**SonarQube Cloud:** analiza el código y permite comprobar si cumple las condiciones establecidas en el Quality Gate.

**JaCoCo:** valida que las pruebas mantengan una cobertura mínima del 75 % de instrucciones.

También se aplicaron algunas medidas de seguridad, como almacenar las contraseñas fuera del repositorio, utilizar secretos de GitHub Actions y ejecutar la aplicación Docker con un usuario sin privilegios.

## 9. Trazabilidad

Para mantener un registro del desarrollo se utilizó Git y GitHub.

Cada cambio realizado queda registrado mediante commits, permitiendo identificar las modificaciones del proyecto.

GitHub Actions también guarda información de las ejecuciones, incluyendo los commits, las pruebas realizadas, el análisis de SonarQube y los resultados del despliegue.

De esta manera se puede revisar cada etapa del pipeline y detectar posibles errores.

## 10. Uso de inteligencia artificial

Durante esta evaluación se utilizó **ChatGPT de OpenAI** como herramienta de apoyo para el desarrollo del proyecto.

Principalmente se utilizó para:

- Comprender los requisitos de la rúbrica y organizar las etapas.
- Interpretar errores de Maven, Docker y SonarQube.

La inteligencia artificial se utilizó como apoyo técnico y de aprendizaje.

## 11. Información final

Este proyecto corresponde a la Evaluación Parcial 2 de Ingeniería DevOps, desarrollada a partir del microservicio OrderManager proporcionado por el profesor.

Se implementaron pruebas automatizadas, controles de calidad, contenedores, persistencia de datos y un pipeline CI/CD que permite automatizar las verificaciones y el despliegue simulado.

Las evidencias y el desarrollo de cada etapa se presentarán en el informe de la evaluación.