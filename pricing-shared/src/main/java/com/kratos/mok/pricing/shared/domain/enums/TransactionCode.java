package com.kratos.mok.pricing.shared.domain.enums;

public enum TransactionCode {
    AGENT_DISTRIBUTOR_WITHDRAWAL(
            "Retrait agent/distributeur",
            TransactionType.WITHDRAWAL
    ),
    DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement agent/distributeur",
            TransactionType.MOK_TRANSFER
    ),
    SUBSCRIBER_WITHDRAWAL(
            "Retrait abonné",
            TransactionType.WITHDRAWAL
    ),
    SUBSCRIBER_P2P_TRANSFER(
            "Transfert P2P abonné",
            TransactionType.P2P_TRANSFER
    ),
    SUBSCRIBER_EXTERNAL_P2P_TRANSFER(
            "Transfert P2P abonné externe",
            TransactionType.EXTERNAL_TRANSFER
    ),
    MERCHANT_PAYMENT(
            "Paiement marchand",
            TransactionType.MERCHANT_PAYMENT
    ),
    SERVICE_PAYMENT(
            "Paiement de services",
            TransactionType.SERVICE_PAYMENT
    ),
    MERCHANT_SETTLEMENT(
            "Reversement marchand",
            TransactionType.BANK_TRANSFER
    ),
    SUPER_DISTRIBUTOR_RECHARGE(
            "Recharge super distributeur",
            TransactionType.BANK_TRANSFER
    ),
    SUPER_DISTRIBUTOR_WITHDRAWAL(
            "Retrait super distributeur",
            TransactionType.BANK_TRANSFER
    ),
    DISTRIBUTOR_DEPOSIT(
            "Dépôt distributeur",
            TransactionType.DEPOSIT
    ),
    SUPER_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P super distributeur",
            TransactionType.P2P_TRANSFER
    ),
    SUPER_DISTRIBUTOR_COMMISSION_SETTLEMENT(
            "Reversement super distributeur",
            TransactionType.MOK_TRANSFER
    ),
    AGENT_DEPOSIT(
            "Dépôt agent",
            TransactionType.DEPOSIT
    ),
    DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P distributeur",
            TransactionType.P2P_TRANSFER
    ),
    DISTRIBUTOR_WITHDRAWAL(
            "Retrait distributeur",
            TransactionType.WITHDRAWAL
    ),
    SUBSCRIBER_DEPOSIT(
            "Dépôt abonné",
            TransactionType.DEPOSIT
    ),
    AGENT_DISTRIBUTOR_P2P_TRANSFER(
            "Transfert P2P agent/distributeur",
            TransactionType.P2P_TRANSFER
    );

    private final String label;
    private final TransactionType transactionType;

    TransactionCode(String label, TransactionType transactionType) {
        this.label = label;
        this.transactionType = transactionType;
    }

    public String label() {
        return label;
    }

    public TransactionType transactionType() {
        return transactionType;
    }
}
