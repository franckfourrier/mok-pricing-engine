package com.kratos.mok.pricing.shared.domain.enums;

public enum TransactionCode {

    AGENT_DISTRIBUTOR_WITHDRAWAL(
            "Retrait agent/distributeur",
            TransactionType.WITHDRAWAL,
            "Agent/distributeur", "Distributeur",
            false, false, false
    ),

    DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement agent/distributeur",
            TransactionType.MOK_TRANSFER,
            "Agent/distributeur (Commissions)", "Abonné / Système ext.",
            false, false, false
    ),

    SUBSCRIBER_WITHDRAWAL(
            "Retrait abonné",
            TransactionType.WITHDRAWAL,
            "Abonné", "Agent/distributeur",
            true, true, true
    ),

    SUBSCRIBER_P2P_TRANSFER(
            "Transfert P2P abonné",
            TransactionType.P2P_TRANSFER,
            "Abonné", "Abonné",
            true, true, true
    ),

    SUBSCRIBER_EXTERNAL_P2P_TRANSFER(
            "Transfert P2P abonné externe",
            TransactionType.EXTERNAL_TRANSFER,
            "Abonné", "Système Ext.",
            true, true, true
    ),

    MERCHANT_PAYMENT(
            "Paiement marchand",
            TransactionType.MERCHANT_PAYMENT,
            "Abonné", "Marchand",
            false, false, false
    ),

    SERVICE_PAYMENT(
            "Paiement de services",
            TransactionType.SERVICE_PAYMENT,
            "Abonné", "Fournisseur de services",
            true, false, true
    ),

    MERCHANT_SETTLEMENT(
            "Reversement marchand",
            TransactionType.BANK_TRANSFER,
            "Marchand", "Abonné / Système Ext.",
            false, false, false
    ),

    SUPER_DISTRIBUTOR_RECHARGE(
            "Recharge super distributeur",
            TransactionType.BANK_TRANSFER,
            "Banque", "Super distributeur",
            false, false, false
    ),

    SUPER_DISTRIBUTOR_WITHDRAWAL(
            "Retrait super distributeur",
            TransactionType.BANK_TRANSFER,
            "Super distributeur", "Banque",
            false, false, false
    ),

    DISTRIBUTOR_DEPOSIT(
            "Dépôt distributeur",
            TransactionType.DEPOSIT,
            "Super distributeur", "Distributeur",
            false, false, false
    ),

    SUPER_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P super distributeur",
            TransactionType.P2P_TRANSFER,
            "Super distributeur", "Super distributeur",
            false, false, false
    ),

    SUPER_DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement super distributeur",
            TransactionType.MOK_TRANSFER,
            "Super distributeur (Commissions)", "Abonné / Système ext.",
            false, false, false
    ),

    AGENT_DEPOSIT(
            "Dépôt agent",
            TransactionType.DEPOSIT,
            "Distributeur", "Agent",
            false, false, false
    ),

    DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P distributeur",
            TransactionType.P2P_TRANSFER,
            "Distributeur", "Distributeur",
            false, false, false
    ),

    DISTRIBUTOR_WITHDRAWAL(
            "Retrait distributeur",
            TransactionType.WITHDRAWAL,
            "Distributeur", "Super distributeur",
            false, false, false
    ),

    SUBSCRIBER_DEPOSIT(
            "Dépôt abonné",
            TransactionType.DEPOSIT,
            "Agent/distributeur", "Abonné",
            false, true, true
    ),

    AGENT_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P agent/distributeur",
            TransactionType.P2P_TRANSFER,
            "Agent/distributeur", "Agent/distributeur",
            false, false, false
    );

    private final String label;
    private final TransactionType transactionType;

    // Nouveaux champs issus de la spécification
    private final String sender;
    private final String receiver;

    private final boolean supportsFees;
    private final boolean supportsTaxes;
    private final boolean supportsCommissions;

    TransactionCode(
            String label,
            TransactionType transactionType,
            String sender,
            String receiver,
            boolean supportsFees,
            boolean supportsTaxes,
            boolean supportsCommissions
    ) {
        this.label = label;
        this.transactionType = transactionType;
        this.sender = sender;
        this.receiver = receiver;
        this.supportsFees = supportsFees;
        this.supportsTaxes = supportsTaxes;
        this.supportsCommissions = supportsCommissions;
    }

    public String label() { return label; }
    public TransactionType transactionType() { return transactionType; }
    public String sender() { return sender; }
    public String receiver() { return receiver; }
    public boolean supportsFees() { return supportsFees; }
    public boolean supportsTaxes() { return supportsTaxes; }
    public boolean supportsCommissions() { return supportsCommissions; }
}