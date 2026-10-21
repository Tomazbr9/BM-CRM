# 2. Levantamento de Requisitos

## 2.1 Requisitos Funcionais

> **Ações que o sistema deve executar.**

* O usuário deve conseguir se cadastrar e fazer login com e-mail e senha.
* O usuário deve conseguir recuperar a senha por e-mail.
* O usuário deve conseguir criar um novo projeto de orçamento.
* Ao criar um projeto, o usuário deve conseguir:

    * Selecionar um cliente já cadastrado; ou
    * Cadastrar um novo cliente, informando:

        * Nome;
        * E-mail;
        * Telefone.
    * Vincular o cliente ao projeto.
* O usuário deve conseguir buscar composições da tabela SINAPI por:

    * Código;
    * Descrição;
    * Categoria.
* O usuário deve conseguir filtrar a busca por:

    * Estado;
    * Mês de referência.
* O usuário deve conseguir adicionar itens (composições) ao orçamento.
* O usuário deve conseguir remover ou editar a quantidade de itens já adicionados ao orçamento.
* O sistema deve calcular automaticamente o total do orçamento com base nos itens adicionados.
* O usuário deve conseguir salvar o orçamento e retomá-lo posteriormente.
* O usuário deve conseguir exportar o orçamento em **PDF**, pronto para apresentação a clientes ou instituições financeiras.
* O sistema deve manter os dados da SINAPI atualizados automaticamente, por estado e por mês.
* O usuário deve conseguir selecionar e alterar o regime de encargos sociais do projeto (**Desonerado** ou **Não Desonerado**) a qualquer momento.
* O usuário deve conseguir inserir uma taxa percentual de **BDI global** para aplicar sobre os custos do orçamento.
* O usuário deve conseguir criar grupos ou etapas, por exemplo:

    * Serviços Preliminares;
    * Fundação;
    * Alvenaria;
    * etc.

  Esses grupos devem permitir estruturar logicamente o orçamento por meio de uma **EAP (Estrutura Analítica do Projeto)**.
* O usuário deve conseguir adicionar itens manuais, provenientes de cotações próprias de mercado ou insumos fora da SINAPI, preenchendo:

    * Descrição;
    * Unidade de medida;
    * Preço unitário.
* O usuário deve conseguir gerar e visualizar a **Curva ABC** do orçamento.
* O usuário deve conseguir exportar o orçamento em formato PDF a planilha de cálculo:
---

## 2.2 Requisitos Não Funcionais

> **Como o sistema deve se comportar**

* **Escala inicial (MVP):** suportar dezenas de usuários simultâneos, sem necessidade de arquitetura de alta escala neste momento.
* **Autenticação:** e-mail + senha, com fluxo de recuperação de senha por e-mail; sessão gerenciada via **JWT (JSON Web Token)**, utilizando arquitetura stateless.
* **Disponibilidade:** sistema 100% web, acessível via navegador, sem dependência de instalação local.
* **Atualização de dados:** novos índices SINAPI devem estar disponíveis em até **24 horas após a publicação oficial da Caixa**.
* **Conformidade:** o sistema deve estar em conformidade com a **LGPD** no tratamento de dados pessoais dos usuários.
* **Tempo de resposta:** definir uma meta específica após a primeira versão estar disponível para medição. Exemplo: buscas de composição devem responder rapidamente mesmo diante de uma base de dados grande.
* Revisitar e detalhar os requisitos conforme o MVP evoluir para produção.

---

## 2.3 Regras de Negócio Complexas

> **Regras de domínio que exigem precisão — não são apenas ações do usuário, são lógicas que precisam estar documentadas sem ambiguidade antes de virar código.**

### 2.3.1 Cálculo de Composição

O cálculo de uma composição deve considerar:

**Composição = Insumos + Mão de Obra**

O cálculo deve seguir rigorosamente a memória de cálculo oficial da SINAPI.

---

### 2.3.2 Variação por Estado e Mês

Os preços de uma composição variam de acordo com:

* Estado;
* Mês de referência.

O sistema deve sempre calcular os valores com base na combinação **(estado + mês)** selecionada pelo usuário.

---

### 2.3.3 Versionamento da SINAPI

Um orçamento salvo deve manter registrado qual versão da tabela SINAPI foi utilizada no cálculo, considerando:

* Estado;
* Mês de referência;
* Regime de encargos sociais.

A atualização posterior da tabela SINAPI **não deve alterar retroativamente** os valores de orçamentos já salvos/fechados.

---

### 2.3.4 Regra de Arredondamento Monetário

Todos os valores monetários devem ser mantidos com **2 casas decimais**, seguindo o padrão monetário brasileiro.

O arredondamento de valores intermediários, como `R$ 10,125`, deve seguir a regra de **arredondamento bancário (round half to even)**, arredondando para o dígito par mais próximo.

Em Java, isso corresponde à utilização de:

```java
RoundingMode.HALF_EVEN
```

aplicado sobre `BigDecimal`.

---

### 2.3.5 Regime de Desoneração

O banco de dados deve armazenar ambas as versões mensais disponibilizadas pela Caixa:

* **Desonerado**;
* **Não Desonerado**.

O cálculo do valor unitário da composição deve apontar rigorosamente para a tabela correspondente ao regime selecionado pelo usuário.

---

### 2.3.6 Cálculo de BDI

O sistema deve diferenciar:

* **Custo Direto**;
* **Preço Final**.

A fórmula utilizada deve ser:

```text
Preço Final = Custo Total × (1 + BDI / 100)
```

Os arquivos exportados em **PDF** e **Excel** devem explicitar claramente a diferença entre:

* Total sem BDI;
* BDI aplicado;
* Total com BDI.

---

# 2.3.7 Geração e Classificação da Curva ABC

## Objetivo

A Curva ABC permite identificar os itens de maior impacto financeiro em um orçamento de obra.

O BuildPrice deverá gerar a Curva ABC utilizando os custos diretos dos itens, ordenados de forma decrescente, calculando sua participação percentual, percentual acumulado e classificação nas categorias A, B ou C.

A funcionalidade pertence ao bounded context `reports`.

## RN-ABC-01 — Base de cálculo

O cálculo deve considerar todos os itens do orçamento, independentemente de sua origem (`SINAPI` ou `MANUAL`) ou grupo da EAP.

O custo direto de cada item será:

`Custo Direto do Item = Quantidade × Preço Unitário Snapshot`

O custo direto total será:

`Custo Direto Total = Σ Custos Diretos dos Itens`

O BDI global não deverá ser incluído na base de cálculo da Curva ABC.

Os valores utilizados devem respeitar os snapshots armazenados no orçamento, garantindo consistência histórica.

## RN-ABC-02 — Ordenação

Os itens devem ser ordenados pelo custo direto em ordem decrescente.

Quando dois ou mais itens possuírem o mesmo custo direto, o sistema deverá utilizar o identificador `budgetItemId` como critério secundário de ordenação, garantindo resultados determinísticos.

## RN-ABC-03 — Participação percentual

A participação individual será calculada por:

`Participação (%) = (Custo Direto do Item / Custo Direto Total) × 100`

## RN-ABC-04 — Percentual acumulado

O percentual acumulado será calculado por:

`Acumulado (%) = (Custo Acumulado até o Item / Custo Direto Total) × 100`

O sistema deve utilizar os custos monetários reais do orçamento para calcular o percentual acumulado, e não a soma das participações percentuais previamente arredondadas.

## RN-ABC-05 — Classificação A, B e C

Serão adotados os seguintes limites fixos para o MVP:

| Classe | Faixa de referência | Impacto |
|---|---|---|
| A | Primeiros 80% do custo acumulado | Alto |
| B | De 80% até 95% | Intermediário |
| C | De 95% até 100% | Baixo |

A classificação será determinada pelo percentual acumulado **antes da inclusão do item**, conforme a regra:

- Classe A: acumulado anterior inferior a 80%.
- Classe B: acumulado anterior maior ou igual a 80% e inferior a 95%.
- Classe C: acumulado anterior maior ou igual a 95%.

O item que ultrapassar um limite permanecerá na classe correspondente ao acumulado anterior à sua inclusão.

**Exemplo:** se o acumulado anterior for 75% e o próximo item elevar o acumulado para 87%, esse item será classificado como A.

Essa convenção evita dividir o impacto financeiro de um item entre classes diferentes.

## RN-ABC-06 — Itens com custos iguais

Itens com o mesmo custo direto positivo devem pertencer à mesma classe.

Para garantir esse comportamento:

1. Os itens serão organizados em conjuntos de custo igual.
2. Todos os itens do mesmo conjunto receberão a classificação determinada pelo acumulado anterior ao conjunto.
3. O custo acumulado será atualizado normalmente após cada item, preservando seus percentuais acumulados individuais.
4. A classificação de um conjunto não poderá ser dividida entre duas classes.

Dessa forma, empates não gerarão diferenças de classificação decorrentes da ordenação secundária por UUID.

## RN-ABC-07 — Orçamentos vazios e custos zerados

Quando o orçamento não possuir itens:

- Custo total igual a zero.
- Lista de resultados vazia.
- Nenhuma classificação gerada.

Quando o custo direto total for zero:

- Participação percentual igual a 0%.
- Percentual acumulado igual a 0%.
- Classificação não atribuída.

Itens individuais com custo zero também não receberão classificação A, B ou C, pois não contribuem financeiramente para o orçamento.

A API poderá representar a ausência de classificação como `null`.

## RN-ABC-08 — Precisão e arredondamento

Os cálculos devem utilizar `BigDecimal`.

Percentuais exibidos ou retornados pela API devem possuir duas casas decimais, com `RoundingMode.HALF_EVEN`.

A determinação das classes deve utilizar valores acumulados sem arredondamento percentual intermediário, para evitar classificações incorretas nos limites de 80% e 95%.

É permitido que a soma das participações individuais já arredondadas seja ligeiramente diferente de 100%.

## RN-ABC-09 — Responsabilidade arquitetural

O módulo `reports` será responsável por:

- Recuperar os dados autorizados do orçamento por meio do contrato público de `budget`.
- Calcular os percentuais e as classificações.
- Disponibilizar os resultados para o frontend.
- Utilizar os resultados em relatórios exportados futuramente.

O módulo não poderá:

- Alterar os itens do orçamento.
- Recalcular custos utilizando versões atuais da SINAPI.
- Acessar diretamente entidades JPA ou repositórios internos do módulo `budget`.
- Persistir alterações no orçamento durante a geração da Curva ABC.

## RN-ABC-10 — Estrutura do resultado

Cada item da Curva ABC deverá apresentar:

- Identificador do item (`budgetItemId`).
- Tipo (`SINAPI` ou `MANUAL`).
- Código, quando disponível.
- Descrição.
- Unidade.
- Quantidade.
- Preço unitário.
- Custo direto.
- Participação percentual.
- Percentual acumulado.
- Classificação (`A`, `B`, `C` ou `null`).

A resposta deverá apresentar também o identificador do orçamento e o custo direto total.

## RN-ABC-11 — Critérios de aceite

A funcionalidade será considerada válida quando:

- Todos os itens de custo positivo forem considerados.
- A ordenação decrescente estiver correta.
- Os percentuais individuais e acumulados estiverem corretos.
- As classes A, B e C respeitarem os limites definidos.
- O item que ultrapassar um limite permanecer na classe anterior.
- Itens com custos iguais receberem a mesma classificação.
- Orçamentos vazios e custos zerados forem tratados corretamente.
- O arredondamento seguir `HALF_EVEN`.
- A geração não alterar o orçamento original.
- Os valores históricos dos snapshots forem preservados.

## Decisão para o MVP

Os limites de 80% e 95% serão fixos no domínio do módulo `reports`.

Não haverá configuração individual desses limites pelo usuário na primeira versão.

Uma futura evolução poderá permitir limites configuráveis, desde que essa alteração seja documentada e acompanhada de testes.
---

### 2.3.8 Isolamento de Itens Próprios

Itens inseridos manualmente pelo usuário não devem sofrer atualização automática durante as viradas de mês da SINAPI.

Os preços devem permanecer **estáticos conforme digitados pelo usuário**, a menos que sejam editados manualmente.

---
