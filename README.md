# training-api

Le projet Spring Boot d'entraînement du module Spring Boot : l'API de la boutique de bougies de Camille (bougies, clientes, commandes). Elle travaille sur la base `candle_shop` de la semaine SQL. Chaque chapitre du cours a sa branche, et chaque branche part de la précédente.

## Ce qu'il te faut

- Le JDK 21 : dans ton terminal, `java -version` affiche `openjdk version "21…`.
- IntelliJ IDEA.
- Le MySQL 8.4 de la semaine SQL, avec le compte `candle_app` (à partir du chapitre 03).
- Docker, pour MongoDB (à partir du chapitre 14) et pour le test de bout en bout (chapitre 15).

## L'ouvrir

1. Clone le dépôt, puis place-toi dans le dossier `training-api`.
2. Dans IntelliJ, clique sur **Open** et choisis le dossier qui contient `pom.xml`.
3. Dans **File > Project Structure > Project**, choisis le JDK 21.

## Charger la base (à partir du chapitre 03)

Spring Boot ne crée aucune table. Les tables viennent des scripts de la semaine SQL, recopiés dans `database/` : `schema.sql` (les 7 tables du MPD) et `seed.sql` (les données de la boutique). Au démarrage, Hibernate vérifie seulement que chaque entité colle à sa table (`spring.jpa.hibernate.ddl-auto=validate`) : s'il manque une colonne, l'appli refuse de démarrer.

Dans un terminal ouvert à la racine du dépôt, dans cet ordre :

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

À partir du chapitre 11, ajoute le script de la sécurité : il ajoute la colonne `role` et le compte de Camille.

```bash
mysql -u root -p < database/security.sql
```

Sous Windows, PowerShell refuse le `<`. Écris à la place `mysql -u root -p -e "source database/schema.sql"`, et de même pour les autres.

Pour repartir de zéro, rejoue les scripts dans le même ordre : `schema.sql` efface toute la base.

## Le compte de l'application

Le back ne se connecte pas avec `root`, mais avec `candle_app`, le compte limité à `SELECT, INSERT, UPDATE, DELETE` que tu as créé au chapitre SQL « Comptes et droits ». Il survit au `DROP DATABASE` de `schema.sql`. S'il n'existe pas sur ta machine, crée-le en `root` :

```sql
CREATE USER 'candle_app'@'%' IDENTIFIED BY 'bougie-2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON candle_shop.* TO 'candle_app'@'%';
```

`bougie-2026` est un mot de passe d'entraînement. Jusqu'au chapitre 07, il est écrit dans `application.properties` ; à partir du chapitre 08, il passe dans le `.env`, hors du dépôt.

## Lancer MongoDB (à partir du chapitre 14)

```bash
docker run -d --name training-api-mongo -p 27017:27017 mongo:8
```

Puis, dans `.env` : `MONGODB_URI_DEV=mongodb://localhost:27017/candle_shop`. La base `candle_shop` de MongoDB et la collection `search_logs` naissent à la première recherche enregistrée.

## Lancer l'appli

```bash
./mvnw spring-boot:run
```

L'API répond sur `http://localhost:8080`. Pour l'arrêter : `Ctrl + C`.

Les tests : `./mvnw verify`. Du chapitre 03 au chapitre 12, ce test démarre toute l'application : MySQL doit tourner, avec la base chargée. Au chapitre 15, le test de bout en bout a besoin de Docker.

À partir du chapitre 08, copie `.env.sample` en `.env` et remplis-le avant de lancer quoi que ce soit.

## Une branche par chapitre

```bash
git switch chapitre-05
```

| Branche | Chapitre |
|---|---|
| `chapitre-01` (et `main`) | Le backend et ton premier projet |
| `chapitre-02` | Beans, IoC et injection |
| `chapitre-03` | Entités et relations JPA |
| `chapitre-04` | Repositories et piège du N+1 |
| `chapitre-05` | Controller et service REST |
| `chapitre-06` | DTOs et validation |
| `chapitre-07` | Les exceptions |
| `chapitre-08` | CORS et variables d'environnement |
| `chapitre-09` | Les transactions |
| `chapitre-10` | Authentification et JWT |
| `chapitre-11` | Le code de sécurité fourni |
| `chapitre-12` | Sécuriser les routes et s'authentifier |
| `chapitre-13` | Tester son back |
| `chapitre-14` | Composant d'accès NoSQL |
| `chapitre-15` | Bonus : optimisations et E2E |
