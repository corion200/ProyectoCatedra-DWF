# Proyecto de Cátedra DWF
---

## Equipo de Desarrollo
| Nombre Completo | Carné | Rol / Módulo Asignado |
| :--- | :--- | :--- |
## Integrantes del Equipo y Responsabilidades
* **Carlos Eduardo Rodriguez Montoya** (RM252980) — *Líder de equipo y desarrollo de servicios backend*
* **Nelson Eduardo Molina Hernández** (MH252987) — *Desarrollo de controladores y APIs REST*
* **Francisco Miguel Serrano Orellana** (SO252952) — *Diseño del esquema de base de datos y persistencia JPA*
* **Johanna Marisela Portillo Anzora** (PA252991) — *Implementación de seguridad y validaciones*
* **Marcelo Augusto Zelaya Colocho** (MA252948) — *Pruebas unitarias y documentación con Swagger*

---

## Tecnologías Utilizadas
* **Lenguaje:** Java 21[cite: 1]
* **Framework:** Spring Boot 3 (Spring MVC, Spring Data JPA, Spring Security)[cite: 1]
* **Base de Datos:** PostgreSQL / MySQL[cite: 1]
* **Seguridad:** JSON Web Tokens (JWT) & BCrypt[cite: 1]
* **Documentación de API:** Swagger / OpenAPI 3[cite: 1]
* **Construcción y Dependencias:** Apache Maven[cite: 1]
* **Pruebas:** JUnit 5 & Mockito[cite: 1]

---

## Arquitectura del Proyecto
El sistema implementa una arquitectura en capas basada en el patrón MVC para asegurar la separación de responsabilidades y la mantenibilidad del código[cite: 1]:

* `Controller`: Manejo de endpoints REST, recepción de DTOs, validaciones con `javax.validation` y respuestas HTTP[cite: 1].
* `Service`: Lógica de negocio (reglas de disponibilidad de espacios, cálculo de costos y manejo de transacciones)[cite: 1].
* `Repository`: Interfaces que extienden de `JpaRepository` para el acceso a datos mediante Spring Data JPA y Hibernate[cite: 1].
* `Exception`: Control centralizado de excepciones con `@ControllerAdvice` y `@ExceptionHandler`[cite: 1].

---

## Pasos para la Configuración e Instalación

### Requisitos Previos
* JDK 21 instalado[cite: 1].
* Gestor de base de datos (PostgreSQL / MySQL)[cite: 1].
* Git.

### Instalación
1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/corion200/ProyectoCatedra-DWF.git](https://github.com/corion200/ProyectoCatedra-DWF.git)
   cd ProyectoCatedra-DWF
