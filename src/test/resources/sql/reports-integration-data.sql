
-- Primeiro usuário, versão SINAPI, projeto e composição
-- Já existentes no fixture do módulo budget:
-- sql/budget-test-data.sql

-- Segundo usuário
INSERT INTO tb_users (
    id, name, email, password_hash, role
) VALUES (
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             'Other Test User',
             'reports-other@example.com',
             'test-hash',
             'USER'
         );

-- Orçamento pertencente ao primeiro usuário
INSERT INTO tb_budgets (
    id,
    project_id,
    sinapi_table_version_id,
    status,
    bdi_percentage
) VALUES (
             '55555555-5555-5555-5555-555555555555',
             '11111111-1111-1111-1111-111111111111',
             '22222222-2222-2222-2222-222222222222',
             'DRAFT',
             20.0000
         );

-- Grupo do orçamento
INSERT INTO tb_budget_groups (
    id, budget_id, parent_group_id, name, sort_order
) VALUES (
             '66666666-6666-6666-6666-666666666666',
             '55555555-5555-5555-5555-555555555555',
             NULL,
             'Alvenaria',
             0
         );

-- Item SINAPI: 2 x R$ 87,45 = R$ 174,90
INSERT INTO tb_budget_items (
    id,
    budget_group_id,
    type,
    composition_id,
    manual_item_id,
    quantity,
    unit_price_snapshot
) VALUES (
             '77777777-7777-7777-7777-777777777777',
             '66666666-6666-6666-6666-666666666666',
             'SINAPI',
             '44444444-4444-4444-4444-444444444444',
             NULL,
             2.0000,
             87.4500
         );
