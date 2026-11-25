-- Datos de prueba para testing de rutas tentativas con depósitos y tarifas reales

-- Tipos de tramo
INSERT INTO tipo (nombre) VALUES ('Origen-Deposito'), ('Deposito-Deposito'), ('Deposito-Destino'), ('Directo');

-- Clientes
INSERT INTO cliente (dni_cliente, nombre, apellido, telefono) VALUES 
(12345678, 'Juan', 'Pérez', '555-0001'),
(87654321, 'María', 'González', '555-0002'),
(11223344, 'Carlos', 'López', '555-0003');

-- Tarifa vigente (importante para cálculo de costos)
INSERT INTO tarifa (valor_litro_combustible, cargo_gestion_por_tramo, fecha_vigencia_desde, fecha_vigencia_hasta) VALUES 
(850.50, 1500.00, '2024-01-01', '2025-12-31'),  -- Tarifa actual vigente (cargo gestión: $1500 por tramo)
(780.00, 1200.00, '2023-01-01', '2023-12-31');  -- Tarifa anterior (no vigente)

-- Depósitos (coordenadas reales de ciudades argentinas)
INSERT INTO deposito (nombre, direccion) VALUES 
('Depósito Rosario', 'Rosario, Santa Fe, Argentina'),
('Depósito Córdoba', 'Córdoba, Córdoba, Argentina'),
('Depósito Villa María', 'Villa María, Córdoba, Argentina'),
('Depósito Buenos Aires', 'Buenos Aires, Argentina');


-- Agregar campo costo_estadia_diario a la tabla deposito si no existe
ALTER TABLE deposito ADD COLUMN IF NOT EXISTS costo_estadia_diario NUMERIC(10,2) DEFAULT 500.00;

-- Actualizar depósitos con costos de estadía
UPDATE deposito SET costo_estadia_diario = 450.00 WHERE nombre = 'Depósito Rosario';
UPDATE deposito SET costo_estadia_diario = 500.00 WHERE nombre = 'Depósito Córdoba';
UPDATE deposito SET costo_estadia_diario = 420.00 WHERE nombre = 'Depósito Villa María';
UPDATE deposito SET costo_estadia_diario = 600.00 WHERE nombre = 'Depósito Buenos Aires';


-- Contenedores
INSERT INTO contenedor (peso, volumen) VALUES 
(5000.00, 15.50 ),
(3500.50, 10.00),
(7800.00, 25.00),
(2100.00, 8.00);

-- Camiones con datos de consumo y costo base para cálculo
INSERT INTO camion (dominio, nombre_transportista, telefono, capacidad_peso, capacidad_volumen, disponibilidad, consumo_por_kilometro) VALUES 
('AB123CD', 'Transporte Rápido SA', '555-1001', 10000.00, 30.00, TRUE, 0.35),
('EF456GH', 'Logística del Sur', '555-1002', 15000.00, 45.00, TRUE, 0.42),
('IJ789KL', 'Carga Express', '555-1003', 8000.00, 25.00, TRUE, 0.30),
('MN012OP', 'Trans Nación', '555-1004', 12000.00, 35.00, FALSE, 0.38);

-- Solicitud de ejemplo (sin ruta asignada aún - para testing de rutas tentativas)
INSERT INTO solicitud (contenedor, cliente, estado_solicitud, origen, destino) VALUES 
(1, 12345678, 'BORRADOR', 'San Francisco, Córdoba, Argentina', 'Buenos Aires, Argentina'),
(2, 87654321, 'BORRADOR', 'Villa Carlos Paz, Córdoba, Argentina', 'Rosario, Santa Fe, Argentina'),
(3, 11223344, 'BORRADOR', 'Río Cuarto, Córdoba, Argentina', 'Córdoba, Córdoba, Argentina');

-- Agregar campo costo_base_por_km a la tabla camion si no existe
ALTER TABLE camion ADD COLUMN IF NOT EXISTS costo_base_por_km NUMERIC(10,2) DEFAULT 50.00;

-- Actualizar camiones con costos base
UPDATE camion SET costo_base_por_km = 45.00 WHERE dominio = 'AB123CD';
UPDATE camion SET costo_base_por_km = 52.00 WHERE dominio = 'EF456GH';
UPDATE camion SET costo_base_por_km = 48.00 WHERE dominio = 'IJ789KL';
UPDATE camion SET costo_base_por_km = 50.00 WHERE dominio = 'MN012OP';



COMMIT;
