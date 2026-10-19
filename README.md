# training-api

Le projet Spring Boot d'entraînement du module Spring Boot : l'API de la boutique de bougies de Camille (produits, clients, commandes). Chaque chapitre du cours a sa branche, et chaque branche part de la précédente.

## Ce qu'il te faut

- Le JDK 21 : dans ton terminal, `java -version` affiche `openjdk version "21…`.
- IntelliJ IDEA.
- Docker, pour MySQL (à partir du chapitre 03).

## L'ouvrir

1. Clone le dépôt, puis place-toi dans le dossier `training-api`.
2. Dans IntelliJ, clique sur **Open** et choisis le dossier qui contient `pom.xml`.
3. Dans **File > Project Structure > Project**, choisis le JDK 21.

## Lancer MySQL (à partir du chapitre 03)

```bash
docker run -d --name training-api-mysql \
  -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=db_eshop \
  -p 3306:3306 mysql:8.4
```

Le lendemain, `docker start training-api-mysql` le relance avec ses données.

Si le port 3306 est déjà pris sur ta machine, remplace `-p 3306:3306` par `-p 3307:3306`, puis mets `3307` dans l'URL de connexion (`application.properties` jusqu'au chapitre 07, `.env` ensuite).

## Lancer l'appli

```bash
./mvnw spring-boot:run
```

L'API répond sur `http://localhost:8080`. Pour l'arrêter : `Ctrl + C`.

Les tests : `./mvnw verify`. Jusqu'au chapitre 12, ce test démarre toute l'application, donc MySQL doit tourner.

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
| `chapitre-14` | Bonus : optimisations et E2E |
