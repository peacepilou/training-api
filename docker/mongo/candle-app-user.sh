#!/bin/sh
# Le compte du back pour MongoDB, comme database/mongo-user.js : readWrite sur candle_shop, rien d'autre.
# L'image mongo le lance une seule fois, au premier démarrage, après avoir créé le compte root
mongosh --quiet -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin candle_shop \
  --eval "db.createUser({ user: '$MONGO_APP_USER', pwd: '$MONGO_APP_PASSWORD', roles: [{ role: 'readWrite', db: 'candle_shop' }] })"
