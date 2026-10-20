// Le compte du back pour MongoDB : lire et écrire dans candle_shop, rien d'autre.
// À lancer une fois, connecté en root ; le back, lui, ne se connecte jamais en root
db.getSiblingDB("candle_shop").createUser({
  user: "candle_app",
  pwd: "journal-2026",
  roles: [{ role: "readWrite", db: "candle_shop" }]
});
