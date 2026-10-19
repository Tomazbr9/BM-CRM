
-- 1. Usuário de teste
INSERT INTO tb_users (
    id,
    name,
    email,
    password_hash,
    role
)
VALUES (
           '33333333-3333-3333-3333-333333333333',
           'Budget Test User',
           'budget-test@example.com',
           'test-hash',
           'USER'
       );

-- 2. Versão SINAPI vinculada ao estado de São Paulo
INSERT INTO tb_sinapi_table_versions (
    id,
    state_id,
    reference_month,
    tax_relief_regime,
    publication_date
)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           (SELECT id FROM tb_states WHERE state_abbreviation = 'SP'),
           DATE '2026-09-01',
           'NOT_EXEMPTED',
           DATE '2026-09-10'
       );

-- 3. Projeto pertencente ao usuário de teste
INSERT INTO tb_projects (
    id,
    user_id,
    client_id,
    name,
    state_id,
    tax_relief_regime,
    bdi_percentage
)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           '33333333-3333-3333-3333-333333333333',
           NULL,
           'Budget Integration Project',
           (SELECT id FROM tb_states WHERE state_abbreviation = 'SP'),
           'NOT_EXEMPTED',
           20.0000
       );

INSERT INTO tb_compositions (
    id,
    version_table_id,
    code,
    description,
    unit,
    unit_cost
)
VALUES (
           '44444444-4444-4444-4444-444444444444',
           '22222222-2222-2222-2222-222222222222',
           '103689',
           'Execução de alvenaria',
           'M2',
           87.450000
       );