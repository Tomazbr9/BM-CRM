CREATE TABLE tb_manual_items (
                                 id UUID PRIMARY KEY,
                                 budget_id UUID NOT NULL,
                                 description VARCHAR(255) NOT NULL,
                                 unit VARCHAR(50) NOT NULL,
                                 unit_price NUMERIC(15, 4) NOT NULL,

                                 CONSTRAINT fk_manual_items_budget
                                     FOREIGN KEY (budget_id)
                                         REFERENCES tb_budgets(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT ck_manual_items_unit_price
                                     CHECK (unit_price >= 0)
);

CREATE INDEX idx_manual_items_budget_id
    ON tb_manual_items(budget_id);


CREATE TABLE tb_budget_items (
                                 id UUID PRIMARY KEY,
                                 budget_group_id UUID NOT NULL,
                                 type VARCHAR(20) NOT NULL,
                                 composition_id UUID,
                                 manual_item_id UUID,
                                 quantity NUMERIC(15, 4) NOT NULL,
                                 unit_price_snapshot NUMERIC(15, 4) NOT NULL,

                                 CONSTRAINT fk_budget_items_group
                                     FOREIGN KEY (budget_group_id)
                                         REFERENCES tb_budget_groups(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_budget_items_composition
                                     FOREIGN KEY (composition_id)
                                         REFERENCES tb_compositions(id),

                                 CONSTRAINT fk_budget_items_manual_item
                                     FOREIGN KEY (manual_item_id)
                                         REFERENCES tb_manual_items(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT ck_budget_items_quantity
                                     CHECK (quantity > 0),

                                 CONSTRAINT ck_budget_items_unit_price_snapshot
                                     CHECK (unit_price_snapshot >= 0),

                                 CONSTRAINT ck_budget_items_reference
                                     CHECK (
                                         (
                                             type = 'SINAPI'
                                                 AND composition_id IS NOT NULL
                                                 AND manual_item_id IS NULL
                                             )
                                             OR
                                         (
                                             type = 'MANUAL'
                                                 AND composition_id IS NULL
                                                 AND manual_item_id IS NOT NULL
                                             )
                                         )
);

CREATE INDEX idx_budget_items_group_id
    ON tb_budget_items(budget_group_id);

CREATE INDEX idx_budget_items_composition_id
    ON tb_budget_items(composition_id);

CREATE INDEX idx_budget_items_manual_item_id
    ON tb_budget_items(manual_item_id);