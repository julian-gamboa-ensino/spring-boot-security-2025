DROP TABLE IF EXISTS cart_vehicle;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS vehicle;

CREATE TABLE vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    modelo VARCHAR(255),
    ano INT,
    preco DECIMAL(19,2),
    color VARCHAR(50),
    disponivel BOOLEAN,
    vendido BOOLEAN
);

CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    expiration_time DATETIME NOT NULL,
    finalizado BOOLEAN NOT NULL,
    expires_at DATETIME
);

CREATE TABLE cart_vehicle (
    cart_id BIGINT,
    vehicle_id BIGINT,
    PRIMARY KEY (cart_id, vehicle_id),
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id)
);
