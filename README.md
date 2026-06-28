#  API Raízes do Nordeste — Sistema Multicanal de Vendas (Back-End)

Este repositório contém a entrega técnica do MVP estruturado para a rede de lanchonetes Raízes do Nordeste. A solução foi projetada sob uma postura de mercado com arquitetura em camadas unificadas, segurança por tokens, persistência de dados real e rastreabilidade total de operações multicanais.

---

##  Como Executar e Validar a API 

Esta suite foi desenvolvida em Java (Spring Boot) e utiliza o **SQLite** como motor de banco de dados persistente em arquivo local, garantindo consistência transacional sem volatidades de resets de memória.

### 1. Configuração do Ambiente e Seed
Antes de rodar a aplicação, certifique-se de configurar as variáveis de ambiente baseando-se no arquivo unificado fornecido na raiz:
* Copie o conteúdo de `.env.example` para um novo arquivo `.env`.
* O banco de dados se auto-inicializará gerando o arquivo `raizes_nordeste.db`.
* **Massa de Dados Inicial (Seed):** O sistema pré-carrega a Unidade de Franquia 2 contendo em estoque **2 unidades do produto Cuscuz Recheado (ID: 101)** para a validação das regras de conflito.

### 2. Importação e Execução da Suite no Postman / Insomnia
1. Baixe o arquivo `raizes_nordeste_postman_collection.json` localizado na raiz deste repositório.
2. Abra o Postman ou Insomnia e clique em **Import**.
3. Selecione o arquivo baixado. A coleção carregará estruturada estritamente em pastas temáticas conforme exigido pelo critério de avaliação (*Auth, Pedidos, Pagamento, Erros e Regras, Logs*).

---

## Ordem Sugerida para Execução dos Testes 

Para o correto acompanhamento dos estados da API e transição do ciclo de vida do pedido, execute as requisições na ordem abaixo:

1. **Pasta Auth -> T01 (Login Válido):** Executar para gerar o Bearer Token necessário para as rotas autenticadas.
2. **Pasta Pedidos -> T06 (Criar Pedido Válido):** Cria um pedido via canal `TOTEM` consumindo 1 item do estoque. O pedido nascerá com status `AGUARDANDO_PAGAMENTO`.
3. **Pasta Pagamento -> T09 (Simular Pagamento Sucesso):** Altera o status do pedido criado anteriormente de forma síncrona para `PAGO` no banco de dados.
4. **Pasta Erros e Regras -> T08 (Criar Pedido com Estoque Insuficiente):** Tente forçar a compra de 5 cuscuz na Unidade 2. O sistema barrará a operação disparando um HTTP Status Code `409 Conflict`.
5. **Pasta Erros e Regras -> T04 (Criar Pedido sem Canal):** Remova o campo `canalPedido` do JSON enviado. O sistema rejeitará a operação retornando um HTTP Status Code `422 Unprocessable Entity` (conforme validado na documentação interativa).

---

##  Segurança e LGPD Aplicada 
* **Controle de Acesso:** Rotas de relatórios da matriz gerencial exigem privilégios de `ADMIN`. Clientes comuns que tentarem o acesso receberão um HTTP Status `403 Forbidden` (Testado no cenário `T03`).
* **Privacidade de Dados:** Senhas de usuários e funcionários são criptografadas antes do armazenamento físico através de algoritmos de hash seguro. Dados pessoais sensíveis nunca são expostos em payloads públicos de Response.

---

## Links Oficiais do Projeto
* **Repositório Público GitHub:** `https://github.com/KamilaEsterOliveira/raizes-nordeste-backend`
* **Contrato Open API/Swagger Interativo:** Pode ser visualizado importando o arquivo do Swagger que está na raiz deste repositório diretamente no [Swagger Editor Online](https://editor.swagger.io/).
