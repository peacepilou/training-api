#!/bin/sh
# Le compte du back, comme au chapitre SQL « Comptes et droits » : SELECT, INSERT, UPDATE, DELETE, jamais root.
# L'image mysql le lance une seule fois, au premier démarrage, après les scripts .sql montés avant lui
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE USER '$DB_USER_DEV'@'%' IDENTIFIED BY '$DB_PASSWORD_DEV';
GRANT SELECT, INSERT, UPDATE, DELETE ON candle_shop.* TO '$DB_USER_DEV'@'%';
SQL
