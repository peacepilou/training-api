-- Boutique de bougies de Camille : les 7 tables du MPD (module Conception, chapitre 09)
DROP DATABASE IF EXISTS candle_shop;
CREATE DATABASE candle_shop;
USE candle_shop;

-- Les tables sans clé étrangère d'abord : une clé étrangère ne peut viser qu'une table qui existe déjà
CREATE TABLE customer (
  id            INT          PRIMARY KEY AUTO_INCREMENT,
  email         VARCHAR(254) NOT NULL UNIQUE,
  first_name    VARCHAR(50)  NOT NULL,
  last_name     VARCHAR(50)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE candle (
  id      INT          PRIMARY KEY AUTO_INCREMENT,
  name    VARCHAR(80)  NOT NULL,
  scent   VARCHAR(40)  NOT NULL,
  size    VARCHAR(10)  NOT NULL,
  price   DECIMAL(6,2) NOT NULL CHECK (price > 0),
  stock   INT          NOT NULL DEFAULT 0 CHECK (stock >= 0),
  on_sale BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE address (
  id          INT          PRIMARY KEY AUTO_INCREMENT,
  street      VARCHAR(120) NOT NULL,
  postcode    CHAR(5)      NOT NULL,
  city        VARCHAR(80)  NOT NULL,
  customer_id INT          NOT NULL,
  CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE CASCADE
);

CREATE TABLE customer_order (
  id          INT         PRIMARY KEY AUTO_INCREMENT,
  ordered_at  DATETIME    NOT NULL,
  status      VARCHAR(20) NOT NULL,
  customer_id INT         NOT NULL,
  address_id  INT,  -- vide si retrait à l'atelier
  CONSTRAINT fk_customer_order_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
  CONSTRAINT fk_customer_order_address  FOREIGN KEY (address_id)  REFERENCES address (id)
);

CREATE TABLE order_line (
  order_id   INT          NOT NULL,
  candle_id  INT          NOT NULL,
  quantity   INT          NOT NULL CHECK (quantity >= 1),
  unit_price DECIMAL(6,2) NOT NULL,  -- le prix payé ce jour-là
  PRIMARY KEY (order_id, candle_id),
  CONSTRAINT fk_order_line_order  FOREIGN KEY (order_id)  REFERENCES customer_order (id) ON DELETE CASCADE,
  CONSTRAINT fk_order_line_candle FOREIGN KEY (candle_id) REFERENCES candle (id)
);

CREATE TABLE review (
  id          INT           PRIMARY KEY AUTO_INCREMENT,
  rating      INT           NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment     VARCHAR(1000) NOT NULL,
  reviewed_at DATETIME      NOT NULL,
  approved    BOOLEAN       NOT NULL DEFAULT FALSE,
  customer_id INT           NOT NULL,
  candle_id   INT           NOT NULL,
  CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE CASCADE,
  CONSTRAINT fk_review_candle   FOREIGN KEY (candle_id)   REFERENCES candle (id)
);

CREATE TABLE favorite (
  customer_id INT      NOT NULL,
  candle_id   INT      NOT NULL,
  added_at    DATETIME NOT NULL,
  PRIMARY KEY (customer_id, candle_id),
  CONSTRAINT fk_favorite_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE CASCADE,
  CONSTRAINT fk_favorite_candle   FOREIGN KEY (candle_id)   REFERENCES candle (id)
);
