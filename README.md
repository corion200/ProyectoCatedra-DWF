# UCA-CFC Connect - Sistema Web Empresarial

## Descripción del Proyecto
**UCA-CFC Connect** es una aplicación web empresarial desarrollada en **Spring Boot** para la asignatura *Desarrollo de Aplicaciones con Web Framework (DWF404)* de la Universidad Don Bosco. 

El sistema centraliza y automatiza los procesos académicos y administrativos del Centro de Formación Continua (CFC) de la UCA mediante una arquitectura moderna orientada a servicios (API REST), resolviendo la gestión de oferta académica, inscripciones, cotizaciones, alquiler de espacios, catering, agenda institucional y pagos.

---

## Integrantes del Equipo y Plan de Trabajo

| Estudiante | Código | Rol / Frente de Trabajo | Módulos / Tareas Asignadas |
| :--- | :--- | :--- | :--- |
| **Carlos Eduardo Rodriguez Montoya** | RM252980 | Líder Técnico / Arquitectura y Seguridad | Estructura del proyecto (Maven, capas), Spring Security + JWT, Módulo 9 (Seguridad: usuarios, roles, permisos, login/logout, recuperación de contraseña), manejo centralizado de excepciones (`@ControllerAdvice`) y configuración de GitHub. |
| **Nelson Eduardo Molina Hernández** | MH252987 | Backend Académico y Clientes | Módulo 1 (Gestión Académica: cursos, diplomados, categorías, modalidades, docentes) y Módulo 2 (Gestión de Clientes). CRUDs, validaciones (`@Valid`, `@NotBlank`, `@Size`) y búsquedas por filtros. |
| **Johanna Marisela Portillo Anzora** | PA252991 | Backend Inscripciones y Cotizaciones | Módulo 3 (Inscripciones con validación de cupo) y Módulo 4 (Cotizaciones con flujo de estados y aprobación). Pruebas unitarias del caso de inscripción con cupo lleno[cite: 3]. |
| **Francisco Miguel Serrano Orellana** | SO252952 | Backend Espacios, Catering y Agenda | Módulo 5 (Alquiler de Espacios), Módulo 6 (Catering) y Módulo 7 (Agenda Institucional). Lógica de validación de traslape de horario (`EspacioOcupadoException`) y pruebas unitarias[cite: 3]. |
| **Marcelo Augusto Zelaya Colocho** | MA252948 | Backend Pagos, Documentación y QA | Módulo 8 (Pagos con métodos y estados), documentación Swagger/OpenAPI de todos los endpoints, paginación/ordenamiento transversal y consolidación de pruebas unitarias (JUnit 5 + Mockito)[cite: 3]. |

---

## Módulos del Sistema
1. **Módulo 1: Gestión Académica** — Cursos, diplomados, categorías, modalidades y docentes; CRUD, activar/inactivar, cupos, fechas, horarios y costos[cite: 3].
2. **Módulo 2: Gestión de Clientes** — Registro, actualización y búsqueda de clientes (personas y empresas) con DUI/NIT[cite: 3].
3. **Módulo 3: Inscripciones** — Inscripción de participantes a cursos con estados Pendiente/Confirmada/Cancelada/Finalizada y control de cupos[cite: 3].
4. **Módulo 4: Cotizaciones** — Solicitud y gestión de cotizaciones para cursos, diplomados, espacios, catering o combinados[cite: 3].
5. **Módulo 5: Alquiler de Espacios** — Administración de espacios físicos (capacidad, precio, disponibilidad, equipamiento)[cite: 3].
6. **Módulo 6: Catering** — Gestión de servicios de alimentación asociados a eventos (coffee break, desayuno, almuerzo, cena, refrigerio)[cite: 3].
7. **Módulo 7: Agenda Institucional** — Calendario unificado de cursos, diplomados, eventos, alquileres y catering, sin conflictos de horario[cite: 3].
8. **Módulo 8: Pagos** — Registro y control financiero de pagos por inscripción, cotización, alquiler o catering, con distintos métodos y estados[cite: 3].
9. **Módulo 9: Seguridad** — Usuarios, roles, permisos, autenticación JWT, login/logout y recuperación de contraseña[cite: 3].

---

## Tecnologías Requeridas
* **Lenguaje:** Java 21[cite: 3]
* **Framework:** Spring Boot 3 (Spring MVC, Spring Data JPA, Hibernate)[cite: 3]
* **Seguridad:** Spring Security & JWT[cite: 3]
* **Base de Datos:** PostgreSQL / MySQL[cite: 3]
* **Documentación:** Swagger / OpenAPI 3 (Springdoc)[cite: 3]
* **Gestión de Proyecto:** Apache Maven[cite: 3]
* **Pruebas:** JUnit 5 & Mockito[cite: 3]

---

## Configuración del Entorno de Desarrollo

### Requisitos Previos
* JDK 21 instalado[cite: 3].
* Servidor MySQL o PostgreSQL[cite: 3].
* Git.

### Pasos para Ejecutar
1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/corion200/ProyectoCatedra-DWF.git](https://github.com/corion200/ProyectoCatedra-DWF.git)
   cd ProyectoCatedra-DWF
