package com.kratos.mok.pricing.shared.domain.enums;

public enum TransactionCode {

    AGENT_DISTRIBUTOR_WITHDRAWAL(
            "Retrait agent/distributeur",
            TransactionType.WITHDRAWAL,
            false, false, false
    ),

    DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement agent/distributeur",
            TransactionType.MOK_TRANSFER,
            false, false, false
    ),

    SUBSCRIBER_WITHDRAWAL(
            "Retrait abonné",
            TransactionType.WITHDRAWAL,
            true, true, true
    ),

    SUBSCRIBER_P2P_TRANSFER(
            "Transfert P2P abonné",
            TransactionType.P2P_TRANSFER,
            true, true, true
    ),

    SUBSCRIBER_EXTERNAL_P2P_TRANSFER(
            "Transfert P2P abonné externe",
            TransactionType.EXTERNAL_TRANSFER,
            true, true, true
    ),

    MERCHANT_PAYMENT(
            "Paiement marchand",
            TransactionType.MERCHANT_PAYMENT,
            false, false, false
    ),

    SERVICE_PAYMENT(
            "Paiement de services",
            TransactionType.SERVICE_PAYMENT,
            true, false, true
    ),

    MERCHANT_SETTLEMENT(
            "Reversement marchand",
            TransactionType.BANK_TRANSFER,
            false, false, false
    ),

    SUPER_DISTRIBUTOR_RECHARGE(
            "Recharge super distributeur",
            TransactionType.BANK_TRANSFER,
            false, false, false
    ),

    SUPER_DISTRIBUTOR_WITHDRAWAL(
            "Retrait super distributeur",
            TransactionType.BANK_TRANSFER,
            false, false, false
    ),

    DISTRIBUTOR_DEPOSIT(
            "Dépôt distributeur",
            TransactionType.DEPOSIT,
            false, false, false
    ),

    SUPER_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P super distributeur",
            TransactionType.P2P_TRANSFER,
            false, false, false
    ),

    SUPER_DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement super distributeur",
            TransactionType.MOK_TRANSFER,
            false, false, false
    ),

    AGENT_DEPOSIT(
            "Dépôt agent",
            TransactionType.DEPOSIT,
            false, false, false
    ),

    DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P distributeur",
            TransactionType.P2P_TRANSFER,
            false, false, false
    ),

    DISTRIBUTOR_WITHDRAWAL(
            "Retrait distributeur",
            TransactionType.WITHDRAWAL,
            false, false, false
    ),

    SUBSCRIBER_DEPOSIT(
            "Dépôt abonné",
            TransactionType.DEPOSIT,
            false, false, true
    ),

    AGENT_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P agent/distributeur",
            TransactionType.P2P_TRANSFER,
            false, false, false
    );

    private final String label;
    private final TransactionType transactionType;

    private final boolean supportsFees;
    private final boolean supportsTaxes;
    private final boolean supportsCommissions;

    TransactionCode(
            String label,
            TransactionType transactionType,
            boolean supportsFees,
            boolean supportsTaxes,
            boolean supportsCommissions
    ) {
        this.label = label;
        this.transactionType = transactionType;
        this.supportsFees = supportsFees;
        this.supportsTaxes = supportsTaxes;
        this.supportsCommissions = supportsCommissions;
    }

    public String label() {
        return label;
    }

    public TransactionType transactionType() {
        return transactionType;
    }

    public boolean supportsFees() {
        return supportsFees;
    }

    public boolean supportsTaxes() {
        return supportsTaxes;
    }

    public boolean supportsCommissions() {
        return supportsCommissions;
    }
}