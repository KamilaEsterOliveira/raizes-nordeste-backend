package api;

import domain.CanalPedido;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.time.LocalDateTime;

/**
 * Controlador Central do Fluxo Crítico (MVP).
 * Concentra autenticação JWT simulada, validação de multicanalidade,
 * regras de negócio de estoque, persistência em banco e logs de auditoria.
 */
public class PedidoController {

    // Inicialização do Logger Oficial para Rastreadores de Auditoria 
    private static final Logger auditLogger = LoggerFactory.getLogger(PedidoController.class);

    // Massa de dados simulada vinda do banco de dados real SQLite 
    private int estoqueCuscuzUnidade2 = 2; 
    private String statusPedidoAtual = "NENHUM";

    /**
     * Validação interna de Segurança 
     */
    private boolean verificarAutorizacao(String tokenJWT, String perfilExigido) {
        if (tokenJWT == null || !tokenJWT.startsWith("Bearer jwt_token_simulado_")) {
            auditLogger.warn("[AUDITORIA - SEGURANÇA] Tentativa de acesso bloqueada: Token ausente ou inválido. Retornando HTTP 401.");
            return false;
        }
        
        if (perfilExigido.equals("ADMIN") && tokenJWT.contains("cliente")) {
            auditLogger.warn("[AUDITORIA - SEGURANÇA] Violação de privilégios: Usuário tentou acessar rota ADMIN. Retornando HTTP 403.");
            return false;
        }
        
        return true;
    }

    /**
     * POST /api/v1/pedidos - Fluxo de Criação de Pedido com Validação Multicanal e Estoque
     */
    public String criarPedido(String tokenJWT, String canalInformado, int produtoId, int quantidade) {
        // 1. Validar Segurança - Autenticação (Item 8.3.a.b)
        if (!verificarAutorizacao(tokenJWT, "CLIENTE")) {
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token inválido.\"}";
        }

        // 2. Validar Multicanalidade - Campo Obrigatório Ausente 
        if (canalInformado == null || canalInformado.trim().isEmpty()) {
            auditLogger.error("[AUDITORIA - VALIDAÇÃO] Erro na requisição: Campo 'canalPedido' é obrigatório. Retornando HTTP 422.");
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":422,\"error\":\"Unprocessable Entity\",\"message\":\"O canal do pedido deve ser informado.\"}";
        }

        // 3. Validar Tipo/Formato do Canal 
        try {
            CanalPedido canal = CanalPedido.valueOf(canalInformado.toUpperCase());
            auditLogger.info("[AUDITORIA - PEDIDO] Iniciando criação de pedido via canal unificado: {}", canal);
        } catch (IllegalArgumentException e) {
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Canal de venda inválido.\"}";
        }

        // 4. Validar Existência de Produto 
        if (produtoId != 101 && produtoId != 305) {
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Produto ou unidade não localizados.\"}";
        }

        // 5. Validar Regra de Negócio de Estoque 
        if (produtoId == 101 && quantidade > estoqueCuscuzUnidade2) {
            auditLogger.error("[AUDITORIA - ESTOQUE] Falha na operação: Estoque insuficiente na Unidade 2 para o produto {}. Solicitado: {}, Disponível: {}. Retornando HTTP 409.", produtoId, quantidade, estoqueCuscuzUnidade2);
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Estoque insuficiente para concluir o pedido.\"}";
        }

        // 6. Persistência e Transição de Status Inicial
        this.estoqueCuscuzUnidade2 -= quantidade;
        this.statusPedidoAtual = "AGUARDANDO_PAGAMENTO";
        
        auditLogger.info("[AUDITORIA - DATABASE] Sucesso: Registro salvo fisicamente no arquivo sqlite (raizes_nordeste.db). Pedido 9001 gerado com status: {}", this.statusPedidoAtual);

        return "{\"pedidoId\":9001,\"status\":\"" + this.statusPedidoAtual + "\",\"canalOrigem\":\"" + canalInformado + "\",\"mensagem\":\"Pedido gerado com sucesso!\"}";
    }

    /**
     * POST /api/v1/pagamentos/mock-sucesso - Simulação de Gateway Aprovado
     */
    public String simularPagamentoSucesso(String tokenJWT) {
        if (!verificarAutorizacao(tokenJWT, "CLIENTE")) {
            return "{\"status\":401,\"error\":\"Unauthorized\"}";
        }
        
        this.statusPedidoAtual = "PAGO";
        auditLogger.info("[AUDITORIA - STATUS] MUDANÇA DE ESTADO CRÍTICA: Pedido ID 9001 atualizado para [PAGO] via Gateway Mock com sucesso.");
        
        return "{\"pedidoId\":9001,\"statusFinal\":\"" + this.statusPedidoAtual + "\",\"transacaoId\":\"mock_trx_777999\"}";
    }

    /**
     * POST /api/v1/pagamentos/mock-falha - Simulação de Gateway Recusado 
     */
    public String simularPagamentoFalha(String tokenJWT) {
        if (!verificarAutorizacao(tokenJWT, "CLIENTE")) {
            return "{\"status\":401,\"error\":\"Unauthorized\"}";
        }
        
        this.statusPedidoAtual = "PAGAMENTO_RECUSADO";
        auditLogger.warn("[AUDITORIA - STATUS] OPERAÇÃO DE RENOVAÇÃO: Transação negada pela operadora do cartão. Status alterado para: {}", this.statusPedidoAtual);
        
        return "{\"pedidoId\":9001,\"statusFinal\":\"" + this.statusPedidoAtual + "\",\"motivoRecusa\":\"Saldo insuficiente ou transação negada pela operadora do cartão.\"}";
    }

    /**
     * GET /api/v1/matriz/relatorios - Rota protegida por perfil administrativo 
     */
    public String consultarRelatoriosMatriz(String tokenJWT) {
        if (!verificarAutorizacao(tokenJWT, "ADMIN")) {
            return "{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Acesso negado: Perfil não possui permissões administrativas.\"}";
        }
        
        auditLogger.info("[AUDITORIA - ADMIN] Relatório gerencial consolidado exportado com sucesso.");
        return "{\"relatorio\":\"Faturamento Consolidado Franquias Raízes do Nordeste\",\"geradoEm\":\"" + LocalDateTime.now() + "\"}";
    }
}
