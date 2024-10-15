-- Alter table to remove unique constraint from administrator_id
ALTER TABLE public.boardrooms DROP CONSTRAINT boardrooms_administrator_id_key;