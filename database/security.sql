-- Sécurité de la boutique (module Spring Boot, chapitre 11) : à lancer en root, après schema.sql et seed.sql.
-- Il modifie une table : candle_app n'a pas le droit ALTER, ce script ne passe donc jamais par le back
USE candle_shop;
SET NAMES utf8mb4;

-- Le rôle de chaque compte : USER pour les clientes, ADMIN pour Camille
ALTER TABLE customer
  ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN'));

-- Mots de passe d'entraînement, hachés en bcrypt : Admin123! (Camille) et Secret123! (Inès).
-- seed.sql ne dit pas quel mot de passe donne chacune de ses empreintes : celui d'Inès est redéfini ici
INSERT INTO customer (email, first_name, last_name, password_hash, role) VALUES
  ('camille@example.com', 'Camille', 'Renard', '$2a$10$GXRb0qkTBpO.yW77ruD8/.KUmHX.Il4a71yQgUGoz7dCP3f1YZ0Je', 'ADMIN');

UPDATE customer
SET password_hash = '$2a$10$vZhrD2slO4Qinr8xYCa8Gu/Itphq7k8fBwy9uYjR9NDqqOWpVj6qu'
WHERE email = 'ines.moreau@gmail.com';
