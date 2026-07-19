-- Rename tables
ALTER TABLE pauta RENAME TO agenda;
ALTER TABLE voto RENAME TO vote;

-- Rename columns in agenda table
ALTER TABLE agenda RENAME COLUMN titulo TO title;
ALTER TABLE agenda RENAME COLUMN descricao TO description;
ALTER TABLE agenda RENAME COLUMN criada_em TO created_at;
ALTER TABLE agenda RENAME COLUMN sessao_aberta_em TO session_opened_at;
ALTER TABLE agenda RENAME COLUMN sessao_fecha_em TO session_closes_at;

-- Rename columns in vote table
ALTER TABLE vote RENAME COLUMN pauta_id TO agenda_id;
ALTER TABLE vote RENAME COLUMN cpf_associado TO member_cpf;
ALTER TABLE vote RENAME COLUMN criado_em TO created_at;

-- Rename constraints
ALTER TABLE vote RENAME CONSTRAINT uk_voto_pauta_cpf TO uk_vote_agenda_member_cpf;

-- Rename index
ALTER INDEX idx_voto_pauta_id RENAME TO idx_vote_agenda_id;

-- Update foreign key if it has a name (H2 may auto-name it)
ALTER TABLE vote RENAME CONSTRAINT fk_voto_pauta_id TO fk_vote_agenda_id;
