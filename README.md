# ms-pagos

Cobro de sesiones y talleres. Cubre **RF-08**. **Zona PCI**: jamás se persisten datos de
tarjeta, solo el token opaco de la pasarela. La confirmación del pago llega por el webhook
público (en producción con firma validada) y gatilla el evento `pago_confirmado`.

**Puerto:** 8085 · **Base de datos:** `pagos_db` · **Swagger:** http://localhost:8085/swagger-ui.html

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/pagos` | Inicia el cobro de una cita/taller (201, queda PENDIENTE) |
| GET | `/api/pagos?estado=` | Lista pagos, filtro opcional por estado |
| GET | `/api/pagos/{id}` | Obtiene un pago (404 si no existe) |
| POST | `/api/pagos/webhook` | Confirmación/rechazo desde la pasarela |
| POST | `/api/pagos/{id}/reembolsos` | Reembolso total o parcial (201; solo pagos CONFIRMADOS) |
| GET | `/api/pagos/{id}/reembolsos` | Lista reembolsos del pago |

Estados: `PENDIENTE`, `CONFIRMADO`, `RECHAZADO`, `REEMBOLSADO`. Tipos: `CITA`, `TALLER`.
Eventos del webhook: `PAGO_CONFIRMADO`, `PAGO_RECHAZADO`.

## Cómo correr localmente

```bash
docker compose -f ../docker-compose.yml up -d   # Postgres + Redis (una sola vez)
mvn spring-boot:run
```

## Arquitectura

Capas `api` → `service` → `repository` → `domain`. Reglas de negocio: el webhook solo
procesa pagos PENDIENTES (409 si ya fue procesado) y los reembolsos acumulados no pueden
superar el monto del pago.
Relación JPA destacada: `Pago` **OneToMany** `Reembolso` (y **ManyToOne** inversa).

## Despliegue

Pensado para **Amazon ECS Fargate** con Aurora Serverless. Imagen: `docker build -t ms-pagos .`
