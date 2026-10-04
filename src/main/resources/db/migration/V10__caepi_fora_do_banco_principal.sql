-- O índice CAEPI passou para um arquivo SQLite próprio.
-- Estas tabelas no banco operacional ficam vazias e não participam mais da carga.

DROP TABLE IF EXISTS caepi_variante;
DROP TABLE IF EXISTS caepi_ca;
DROP TABLE IF EXISTS caepi_carga;
