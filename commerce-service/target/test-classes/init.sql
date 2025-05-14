DROP TABLE IF EXISTS cart_vehicle;
DROP TABLE IF EXISTS sale_vehicles;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS vehicles;

CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modelo VARCHAR(255),
    ano INT CHECK (ano >= 1900),
    preco DECIMAL(10,2) CHECK (preco >= 0),
    color ENUM('BRANCA', 'PRATA', 'PRETA') NOT NULL,
    disponivel BOOLEAN NOT NULL,
    vendido BOOLEAN NOT NULL,
    version BIGINT,
    carrinho_id BIGINT,
    carrinho_timestamp BIGINT
);

CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'COMPLETED') NOT NULL,
    created_at DATETIME NOT NULL,
    expiration_time DATETIME NOT NULL,
    finalizado BOOLEAN NOT NULL,
    expires_at DATETIME
);

CREATE TABLE cart_vehicle (
    cart_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    PRIMARY KEY (cart_id, vehicle_id),
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE TABLE sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    valor_total DECIMAL(38,2) NOT NULL,
    data_venda DATETIME,
    tipo ENUM('ONLINE', 'FISICA') NOT NULL
);

CREATE TABLE sale_vehicles (
    sale_id BIGINT NOT NULL,
    vehicle_id BIGINT,
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);
