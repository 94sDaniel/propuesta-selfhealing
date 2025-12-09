CREATE USER healenium_user WITH ENCRYPTED PASSWORD 'YDk2nmNs4s9aCP6K';
CREATE DATABASE healenium OWNER healenium_user;
GRANT ALL PRIVILEGES ON DATABASE healenium TO healenium_user;
CREATE SCHEMA healenium AUTHORIZATION healenium_user;
