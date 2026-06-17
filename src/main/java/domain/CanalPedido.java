package domain;

/**
 * Item 5.5 / 7.2 - ENUM que blinda e centraliza os canais de venda permitidos
 * no ecossistema multicanal da lanchonete.
 */
public enum CanalPedido {
    APP,
    TOTEM,
    BALCAO,
    PICKUP,
    WEB
}
