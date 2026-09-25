-- =====================================================================
-- UCA-CFC CONNECT | Centro de Formación Continua | UDB
-- Motor: MySQL 8.0+ / MariaDB 10.4+  (XAMPP)
-- =====================================================================
CREATE DATABASE IF NOT EXISTS uca_cfc_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE uca_cfc_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS usuarios, roles, pagos, catering_servicios, alquileres,
    cotizaciones, inscripciones, curso_docente, cursos, docentes, clientes,
    espacios, modalidades, categorias;
SET FOREIGN_KEY_CHECKS = 1;

-- ─────────────────── 0. SEGURIDAD (Módulo 9: usuarios y roles) ──────
CREATE TABLE roles (
    id       BIGINT      AUTO_INCREMENT PRIMARY KEY,
    nombre   VARCHAR(20) NOT NULL,
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuarios (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    correo         VARCHAR(150) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    nombre         VARCHAR(200) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    rol_id         BIGINT       NOT NULL,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES roles (id),
    INDEX idx_usuarios_rol (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (nombre) VALUES
    ('ADMIN'), ('RECEPCIONISTA'), ('CLIENTE'), ('CONTABILIDAD');
-- (los usuarios demo los crea la app al primer arranque, con BCrypt)

-- ─────────────────────────── 1. CATÁLOGOS ───────────────────────────
CREATE TABLE categorias (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    descripcion         VARCHAR(500),
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE modalidades (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100)  NOT NULL,
    descripcion         VARCHAR(500),
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_modalidades_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────── 2. GESTIÓN ACADÉMICA ───────────────────────
CREATE TABLE docentes (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    dui                 VARCHAR(10)  NOT NULL,
    nit                 VARCHAR(17),
    correo              VARCHAR(150) NOT NULL,
    telefono            VARCHAR(15),
    titulo_profesional  VARCHAR(200),
    especialidad        VARCHAR(200),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_docentes_dui    UNIQUE (dui),
    CONSTRAINT uq_docentes_nit    UNIQUE (nit),
    CONSTRAINT uq_docentes_correo UNIQUE (correo),
    INDEX idx_docentes_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cursos y Diplomados (discriminados por 'tipo')
CREATE TABLE cursos (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    nombre              VARCHAR(200)  NOT NULL,
    descripcion         VARCHAR(1000),
    tipo                VARCHAR(20)   NOT NULL,
    categoria_id        BIGINT        NOT NULL,
    modalidad_id        BIGINT        NOT NULL,
    horas               INT           NOT NULL,
    precio              DECIMAL(10,2) NOT NULL,
    cupo_maximo         INT           NOT NULL,
    fecha_inicio        DATE          NOT NULL,
    fecha_fin           DATE          NOT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PROGRAMADO',
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cursos_codigo      UNIQUE (codigo),
    CONSTRAINT fk_cursos_categoria   FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT fk_cursos_modalidad   FOREIGN KEY (modalidad_id) REFERENCES modalidades (id),
    CONSTRAINT ck_cursos_tipo   CHECK (tipo IN ('CURSO','DIPLOMADO')),
    CONSTRAINT ck_cursos_horas  CHECK (horas > 0),
    CONSTRAINT ck_cursos_precio CHECK (precio >= 0),
    CONSTRAINT ck_cursos_cupo   CHECK (cupo_maximo > 0),
    CONSTRAINT ck_cursos_estado CHECK (estado IN ('PROGRAMADO','EN_CURSO','FINALIZADO','CANCELADO')),
    CONSTRAINT ck_cursos_fechas CHECK (fecha_fin >= fecha_inicio),
    INDEX idx_cursos_categoria (categoria_id),
    INDEX idx_cursos_modalidad (modalidad_id),
    INDEX idx_cursos_estado    (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Relación N:M Curso ↔ Docente
CREATE TABLE curso_docente (
    curso_id   BIGINT NOT NULL,
    docente_id BIGINT NOT NULL,
    CONSTRAINT pk_curso_docente PRIMARY KEY (curso_id, docente_id),
    CONSTRAINT fk_cd_curso   FOREIGN KEY (curso_id)   REFERENCES cursos (id)   ON DELETE CASCADE,
    CONSTRAINT fk_cd_docente FOREIGN KEY (docente_id) REFERENCES docentes (id) ON DELETE CASCADE,
    INDEX idx_curso_docente_docente (docente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ───────────────────────── 3. CLIENTES ──────────────────────────────
CREATE TABLE clientes (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tipo_cliente        VARCHAR(10)  NOT NULL,
    nombre              VARCHAR(200) NOT NULL,
    dui                 VARCHAR(10),
    nit                 VARCHAR(17)  NOT NULL,
    correo              VARCHAR(150) NOT NULL,
    telefono            VARCHAR(15)  NOT NULL,
    direccion           VARCHAR(300),
    contacto_nombre     VARCHAR(150),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_clientes_dui    UNIQUE (dui),
    CONSTRAINT uq_clientes_nit    UNIQUE (nit),
    CONSTRAINT uq_clientes_correo UNIQUE (correo),
    CONSTRAINT ck_clientes_tipo CHECK (tipo_cliente IN ('PERSONA','EMPRESA')),
    INDEX idx_clientes_tipo (tipo_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────── 4. INSCRIPCIONES ──────────────────────────
CREATE TABLE inscripciones (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)  NOT NULL,
    cliente_id          BIGINT       NOT NULL,
    curso_id            BIGINT       NOT NULL,
    fecha_inscripcion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_inscripciones_codigo UNIQUE (codigo),
    CONSTRAINT fk_insc_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_insc_curso   FOREIGN KEY (curso_id)   REFERENCES cursos (id),
    CONSTRAINT ck_insc_estado CHECK (estado IN ('PENDIENTE','CONFIRMADA','CANCELADA','FINALIZADA')),
    CONSTRAINT uq_insc_cliente_curso UNIQUE (cliente_id, curso_id),   -- red de seguridad de la regla anti-duplicados
    INDEX idx_insc_cliente (cliente_id),
    INDEX idx_insc_curso   (curso_id),
    INDEX idx_insc_estado  (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────── 5. ALQUILER DE ESPACIOS ───────────────────────
CREATE TABLE espacios (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    nombre              VARCHAR(150)  NOT NULL,
    tipo                VARCHAR(20)   NOT NULL,
    capacidad           INT           NOT NULL,
    precio_hora         DECIMAL(10,2) NOT NULL,
    equipamiento        VARCHAR(1000),
    disponible          BOOLEAN       NOT NULL DEFAULT TRUE,
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_espacios_codigo UNIQUE (codigo),
    CONSTRAINT ck_espacios_tipo      CHECK (tipo IN ('AUDITORIO','SALA','LABORATORIO','TALLER','CANCHA','OTRO')),
    CONSTRAINT ck_espacios_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_espacios_precio    CHECK (precio_hora >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────── 6. COTIZACIONES ────────────────────────────
CREATE TABLE cotizaciones (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    cliente_id          BIGINT        NOT NULL,
    tipo                VARCHAR(30)   NOT NULL,
    descripcion         VARCHAR(1000) NOT NULL,
    fecha_evento        DATE,
    monto_estimado      DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_cotizaciones_codigo UNIQUE (codigo),
    CONSTRAINT fk_cot_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT ck_cot_tipo   CHECK (tipo IN ('CURSO_EMPRESARIAL','DIPLOMADO','ALQUILER_ESPACIO','CATERING')),
    CONSTRAINT ck_cot_monto  CHECK (monto_estimado >= 0),
    CONSTRAINT ck_cot_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','APROBADA','RECHAZADA')),
    INDEX idx_cot_cliente (cliente_id),
    INDEX idx_cot_estado  (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE alquileres (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    espacio_id          BIGINT        NOT NULL,
    cliente_id          BIGINT        NOT NULL,
    cotizacion_id       BIGINT,
    fecha_evento        DATE          NOT NULL,
    hora_inicio         TIME          NOT NULL,
    hora_fin            TIME          NOT NULL,
    costo_total         DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_alquileres_codigo UNIQUE (codigo),
    CONSTRAINT fk_alq_espacio   FOREIGN KEY (espacio_id)    REFERENCES espacios (id),
    CONSTRAINT fk_alq_cliente   FOREIGN KEY (cliente_id)    REFERENCES clientes (id),
    CONSTRAINT fk_alq_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones (id),
    CONSTRAINT ck_alq_costo  CHECK (costo_total >= 0),
    CONSTRAINT ck_alq_estado CHECK (estado IN ('PENDIENTE','CONFIRMADA','CANCELADA','FINALIZADA')),
    CONSTRAINT ck_alq_horas  CHECK (hora_fin > hora_inicio),
    INDEX idx_alq_espacio_fecha (espacio_id, fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────── 7. CATERING ────────────────────────────
CREATE TABLE catering_servicios (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    cliente_id          BIGINT        NOT NULL,
    cotizacion_id       BIGINT,
    tipo_servicio       VARCHAR(20)   NOT NULL,
    numero_asistentes   INT           NOT NULL,
    menu                VARCHAR(1000) NOT NULL,
    fecha_evento        DATE          NOT NULL,
    hora_entrega        TIME          NOT NULL,
    lugar               VARCHAR(255)  NOT NULL,
    precio_por_persona  DECIMAL(10,2) NOT NULL,
    costo_total         DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_catering_codigo UNIQUE (codigo),
    CONSTRAINT fk_cat_cliente     FOREIGN KEY (cliente_id)    REFERENCES clientes (id),
    CONSTRAINT fk_cat_cotizacion  FOREIGN KEY (cotizacion_id) REFERENCES cotizaciones (id),
    CONSTRAINT ck_cat_tipo       CHECK (tipo_servicio IN ('COFFEE_BREAK','ALMUERZO','CENA','SNACK','BANQUETE','OTRO')),
    CONSTRAINT ck_cat_asistentes CHECK (numero_asistentes > 0),
    CONSTRAINT ck_cat_precio     CHECK (precio_por_persona >= 0),
    CONSTRAINT ck_cat_costo      CHECK (costo_total >= 0),
    CONSTRAINT ck_cat_estado     CHECK (estado IN ('PENDIENTE','CONFIRMADO','CANCELADO','FINALIZADO')),
    INDEX idx_cat_fecha (fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────── 8. PAGOS ──────────────────────────────
CREATE TABLE pagos (
    id                  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(20)   NOT NULL,
    inscripcion_id      BIGINT,
    cotizacion_id       BIGINT,
    alquiler_id         BIGINT,
    catering_id         BIGINT,
    monto               DECIMAL(12,2) NOT NULL,
    metodo_pago         VARCHAR(20)   NOT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_comprobante  VARCHAR(50),
    observaciones       VARCHAR(500),
    fecha_creacion      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME      ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pagos_codigo UNIQUE (codigo),
    CONSTRAINT fk_pago_insc FOREIGN KEY (inscripcion_id) REFERENCES inscripciones (id),
    CONSTRAINT fk_pago_cot  FOREIGN KEY (cotizacion_id)  REFERENCES cotizaciones (id),
    CONSTRAINT fk_pago_alq  FOREIGN KEY (alquiler_id)    REFERENCES alquileres (id),
    CONSTRAINT fk_pago_cat  FOREIGN KEY (catering_id)    REFERENCES catering_servicios (id),
    CONSTRAINT ck_pago_monto  CHECK (monto > 0),
    CONSTRAINT ck_pago_metodo CHECK (metodo_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA','DEPOSITO')),
    CONSTRAINT ck_pago_estado CHECK (estado IN ('PENDIENTE','PARCIAL','PAGADO')),
    -- REGLA: un pago apunta a EXACTAMENTE un documento
    CONSTRAINT ck_pago_documento CHECK (
        (IF(inscripcion_id IS NOT NULL, 1, 0) +
         IF(cotizacion_id  IS NOT NULL, 1, 0) +
         IF(alquiler_id    IS NOT NULL, 1, 0) +
         IF(catering_id    IS NOT NULL, 1, 0)) = 1
    ),
    INDEX idx_pago_insc   (inscripcion_id),
    INDEX idx_pago_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ───────────────── 9. DATOS INICIALES (para las demos) ──────────────
INSERT INTO modalidades (nombre, descripcion) VALUES
    ('Presencial', 'Clases en las instalaciones del CFC'),
    ('Virtual',    'Clases sincrónicas en línea'),
    ('Híbrida',    'Sesiones presenciales y virtuales combinadas');

INSERT INTO categorias (nombre, descripcion) VALUES
    ('Tecnología e Informática', 'Programación, redes, datos y ofimática'),
    ('Gestión Empresarial',      'Administración, contabilidad y RRHH'),
    ('Diseño y Creatividad',     'Diseño gráfico, UX/UI y multimedia');

INSERT INTO docentes (nombres, apellidos, dui, nit, correo, telefono, titulo_profesional, especialidad) VALUES
    ('Carlos',    'Martínez Huezo',  '01234567-8', '0123-456789-101-8', 'cmartinez@udb.edu.sv', '2222-1111', 'Ingeniero en Informática', 'Desarrollo Web Full Stack'),
    ('Ana Sofia', 'Ramírez de Cruz', '08765432-1', '0876-543210-102-1', 'aramirez@udb.edu.sv',  '2222-2222', 'Licenciada en Diseño',     'UX/UI y Multimedia');

INSERT INTO espacios (codigo, nombre, tipo, capacidad, precio_hora, equipamiento) VALUES
    ('AUD-01', 'Auditorio Principal',    'AUDITORIO',   300, 150.00, 'Proyector, sistema de sonido, A/C'),
    ('SAL-02', 'Sala de Capacitación 2', 'SALA',         30,  40.00, 'Proyector, pizarra acrílica, A/C'),
    ('LAB-01', 'Laboratorio de Cómputo', 'LABORATORIO',  25,  60.00, '25 PCs i5, proyector, A/C');

INSERT INTO clientes (tipo_cliente, nombre, dui, nit, correo, telefono, direccion, contacto_nombre) VALUES
    ('PERSONA', 'María José Rivera',               '04567891-2', '0456-789102-101-4', 'mrivera@gmail.com',    '7777-1234', 'Col. Escalón, San Salvador',          NULL),
    ('EMPRESA', 'Corporación Tecnosur S.A. de C.V.', NULL,       '0614-250987-112-3', 'rrhh@tecnosur.com.sv', '2525-6000', 'Santa Elena, Antiguo Cuscatlán',      'Lic. Jorge Pineda');

-- CUR-DEMO1 con cupo=2 A PROPÓSITO: para demostrar "Cupo agotado (2/2)"
INSERT INTO cursos (codigo, nombre, descripcion, tipo, categoria_id, modalidad_id, horas, precio, cupo_maximo, fecha_inicio, fecha_fin) VALUES
    ('CUR-DEMO1', 'Java Básico',                 'Fundamentos de programación en Java', 'CURSO',     1, 1, 40,  250.00,  2, '2026-10-01', '2026-11-15'),
    ('DIP-DAT01', 'Diplomado en Ciencia de Datos','Python, estadística y ML',           'DIPLOMADO', 1, 3, 160, 950.00, 20, '2026-10-01', '2027-02-28');

INSERT INTO curso_docente (curso_id, docente_id) VALUES (1, 1), (2, 1);
