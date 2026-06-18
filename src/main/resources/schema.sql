--ATENTOS A ESTO POR FAVOR, ESTE SCHEMA DESPUES SE TIENE QUE BORRAR Y LLEVAR A LA BASE DE DATOS DE MYSQL, ES DECIR, ESTE CODIGO VA EN LA BASE DE DATOS Y DE AHI
--HACEMOS LA CONEXION
--QUIEN SE QUIERA HACER CARGO QUE DIGA, TAMBIEN SI EL TIEMPO NOS DA
-- PODEMOS HACER UNA BASE DE DATOS CONTINUAS (QUE LOS DATOS SEAN EN TIEMPO REAL)


-- ============================================================================
-- 1-Creamos la base de Datos para que después al conectarla a MySQL quede todo hecho
-- ============================================================================
CREATE DATABASE IF NOT EXISTS food_store_db;
USE food_store_db; --Asi se va a llamar nuestra base de datos, acuerdense

-- Si la cagamos, borramos todo XD
DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS categoria;

-- ============================================================================
-- 2-Creamos las tablas correspondientes a cada categoria
-- ============================================================================

-- Tabla Categoria (Hereda de Base: id, eliminado, created_at)
CREATE TABLE categoria (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
eliminado BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
nombre VARCHAR(100) NOT NULL,
descripcion VARCHAR(255) NOT NULL
);

-- Tabla de Usuario (Hereda de Base. El mail debe ser único en DB)
CREATE TABLE usuario (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
eliminado BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
nombre VARCHAR(100) NOT NULL,
apellido VARCHAR(100) NOT NULL,
mail VARCHAR(150) NOT NULL UNIQUE,
celular VARCHAR(50) NOT NULL,
contrasena VARCHAR(255) NOT NULL,
rol VARCHAR(20) NOT NULL -- <- Aca guardamos los roles de USUARIO u otros que hayan
);

-- Tabla de Producto (Relación N:1 con Categoria mediante FK)
CREATE TABLE producto (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
eliminado BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
nombre VARCHAR(150) NOT NULL,
precio DOUBLE NOT NULL,
descripcion TEXT,
stock INT NOT NULL,
imagen VARCHAR(255),
disponible BOOLEAN NOT NULL DEFAULT TRUE,
categoria_id BIGINT NOT NULL,
CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria(id)
);

-- Tabla de Pedido (Relación N:1 con Usuario mediante FK)
CREATE TABLE pedido (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
eliminado BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha DATE NOT NULL,
estado VARCHAR(30) NOT NULL, -- String del Enum (PENDIENTE, CONFIRMADO, etc.)
total DOUBLE NOT NULL DEFAULT 0.0,
forma_pago VARCHAR(30) NOT NULL, -- String del Enum (TARJETA, EFECTIVO, etc.)
usuario_id BIGINT NOT NULL,
CONSTRAINT fk_pedido_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- Tabla de DetallePedido (Composición. Relación N:1 con Pedido y N:1 con Producto)
CREATE TABLE detalle_pedido (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
eliminado BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
cantidad INT NOT NULL,
subtotal DOUBLE NOT NULL,
pedido_id BIGINT NOT NULL,
producto_id BIGINT NOT NULL,
CONSTRAINT fk_detalle_pedido FOREIGN KEY (pedido_id) REFERENCES pedido(id),
CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- ============================================================================
-- 3. Acá lo que hicimos fue hacer unos "datos semilla" para que la BD no se cree vacia y nos reviente todo
-- ============================================================================

-- Categorías iniciales
INSERT INTO categoria (nombre, descripcion) VALUES
('Hamburguesas', 'Burgers completas con papas fritas fijas'),
('Pizzas', 'Pizzas al horno de barro con masa madre');



-- Productos iniciales (asociados a las categorías anteriores mediante su ID)
INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, categoria_id) VALUES
('Cheeseburger Doble', 4500.0, 'Doble carne, doble queso cheddar y aderezo especial', 20, 'burger_doble.jpg', TRUE, 1),
('Pizza Muzzarella Especial', 6000.0, 'Salsa de tomate artesanal, muzzarella y aceitunas negras', 15, 'pizza_muzza.jpg', TRUE, 2),
('Papas con Cheddar y Bacon', 2500.0, 'Porción grande de papas rústicas con salsa de queso y tocino crocante', 30, 'papas_cheddar.jpg', TRUE, 1);



-- Usuarios iniciales (Uno Admin y uno Usuario común)
INSERT INTO usuario (nombre, apellido, mail, celular, contrasena, rol) VALUES
('Mateo', 'Gomez', 'mateo@mail.com', '2615551234', 'admin123', 'ADMIN'),
('Carlos', 'Perez', 'carlos@mail.com', '1164839201', 'user123', 'USUARIO');