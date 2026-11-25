
DROP TABLE IF EXISTS tramo CASCADE;
DROP TABLE IF EXISTS ruta CASCADE;
DROP TABLE IF EXISTS solicitud CASCADE;
DROP TABLE IF EXISTS contenedor CASCADE;
DROP TABLE IF EXISTS deposito CASCADE;
DROP TABLE IF EXISTS punto CASCADE;
DROP TABLE IF EXISTS camion CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS tipo CASCADE;
DROP TABLE IF EXISTS tarifa CASCADE;

CREATE TABLE tipo (
  id_tipo INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre  VARCHAR(50) NOT NULL
);

CREATE TABLE cliente (
  dni_cliente INT PRIMARY KEY,
  nombre      VARCHAR(120) NOT NULL,
  apellido    VARCHAR(120) NOT NULL,
  telefono    VARCHAR(20)
);

CREATE TABLE tarifa (
  id_tarifa INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  valor_litro_combustible NUMERIC(10,2),
  cargo_gestion_por_tramo NUMERIC(10,2),
  fecha_vigencia_desde DATE,
  fecha_vigencia_hasta DATE
);

CREATE TABLE deposito (
  id_deposito INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre      VARCHAR(120) NOT NULL UNIQUE,
  direccion   VARCHAR(200)
);

CREATE TABLE contenedor (
  id_contenedor INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  peso          NUMERIC(10,2),
  volumen       NUMERIC(10,2)
);

CREATE TABLE camion (
  dominio               VARCHAR(10) PRIMARY KEY,
  nombre_transportista  VARCHAR(120) NOT NULL,
  telefono              VARCHAR(20),
  capacidad_peso        NUMERIC(10,2) NOT NULL,
  capacidad_volumen     NUMERIC(10,2) NOT NULL,
  disponibilidad        BOOLEAN NOT NULL DEFAULT TRUE,
  consumo_por_kilometro NUMERIC(10,3) NOT NULL,
  costo_base_por_km     NUMERIC(10,2) NOT NULL DEFAULT 50.00
);

CREATE TABLE punto (
  id_punto          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  direccion_textual TEXT,
  latitud           NUMERIC(9,6),
  longitud          NUMERIC(9,6),
  deposito          INT,
  CONSTRAINT fk_punto_deposito FOREIGN KEY (deposito) REFERENCES deposito(id_deposito)
);

CREATE TABLE ruta (
  id_ruta             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cantidad_tramos     INT NOT NULL,
  cantidad_depositos  INT NOT NULL
);

CREATE TABLE solicitud (
  id_solicitud     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  contenedor       INT NOT NULL,
  cliente          INT NOT NULL,
  costo_estimado   NUMERIC(12,2),
  tiempo_estimado  DATE,
  costo_final      NUMERIC(12,2),
  tiempo_real      DATE,
  estado_solicitud VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
  origen           VARCHAR(255),
  destino          VARCHAR(255),
  ruta             INT UNIQUE,
  CONSTRAINT fk_solicitud_contenedor FOREIGN KEY (contenedor) REFERENCES contenedor(id_contenedor),
  CONSTRAINT fk_solicitud_cliente FOREIGN KEY (cliente) REFERENCES cliente(dni_cliente),
  CONSTRAINT fk_solicitud_ruta FOREIGN KEY (ruta) REFERENCES ruta(id_ruta) ON DELETE SET NULL
);

CREATE TABLE tramo (
  id_tramo          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  origen            INT NOT NULL,
  destino           INT NOT NULL,
  id_tipo           INT NOT NULL,
  costo_aproximado  NUMERIC(12,2),
  costo_real        NUMERIC(12,2),
  fecha_hora_inicio_estimada TIMESTAMP,
  fecha_hora_fin_estimada    TIMESTAMP,
  fecha_hora_inicio TIMESTAMP,
  fecha_hora_fin    TIMESTAMP,
  kilometros_tramo  NUMERIC(10,2),
  dominio           VARCHAR(10),
  ruta              INT NOT NULL,
  estado_tramo      VARCHAR(20) NOT NULL,
  CONSTRAINT fk_tramo_origen  FOREIGN KEY (origen)  REFERENCES punto(id_punto),
  CONSTRAINT fk_tramo_destino FOREIGN KEY (destino) REFERENCES punto(id_punto),
  CONSTRAINT fk_tramo_tipo    FOREIGN KEY (id_tipo) REFERENCES tipo(id_tipo),
  CONSTRAINT fk_tramo_camion  FOREIGN KEY (dominio) REFERENCES camion(dominio),
  CONSTRAINT fk_tramo_ruta    FOREIGN KEY (ruta)    REFERENCES ruta(id_ruta) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_solicitud_cliente    ON solicitud(cliente);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado     ON solicitud(estado_solicitud);
CREATE INDEX IF NOT EXISTS idx_tramo_origen         ON tramo(origen);
CREATE INDEX IF NOT EXISTS idx_tramo_destino        ON tramo(destino);
CREATE INDEX IF NOT EXISTS idx_tramo_tipo           ON tramo(id_tipo);
CREATE INDEX IF NOT EXISTS idx_tramo_camion         ON tramo(dominio);
CREATE INDEX IF NOT EXISTS idx_tramo_ruta           ON tramo(ruta);
CREATE INDEX IF NOT EXISTS idx_tramo_estado         ON tramo(estado_tramo);
