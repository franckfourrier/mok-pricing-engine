package com.kratos.mok.pricing.ledger.infrastructure.mapper;

import com.kratos.mok.pricing.ledger.domain.LedgerAccount;
import com.kratos.mok.pricing.ledger.domain.LedgerEntry;
import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerAccountEntity;
import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerEntryEntity;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class LedgerMapper {

    public LedgerAccount toDomain(LedgerAccountEntity e) {
        return LedgerAccount.reconstitute(
                e.getId(),
                e.getCode(),
                e.getName(),
                e.getCurrency(),
                e.getCreatedAt()
        );
    }

    public LedgerEntry toDomain(LedgerEntryEntity e) {
        return new LedgerEntry(
                e.getExternalTxId(),
                e.getLineNo(),
                e.getOccurredAt(),
                e.getAccountCode(),
                e.getDirection(),
                Money.of(e.getAmount(), e.getCurrency()),
                e.getKind(),
                e.getPolicyId(),
                e.getDescription(),
                e.getCreatedBy(),
                e.getCreatedAt()
        );
    }

    public LedgerEntryEntity toEntity(LedgerEntry d) {
        LedgerEntryEntity e = new LedgerEntryEntity();
        e.setExternalTxId(d.externalTxId());
        e.setLineNo(d.lineNo());
        e.setOccurredAt(d.occurredAt());
        e.setAccountCode(d.accountCode());
        e.setDirection(d.direction());
        e.setAmount(d.amount().amount());
        e.setCurrency(d.amount().currency());
        e.setKind(d.kind());
        e.setPolicyId(d.policyId());
        e.setDescription(d.description());
        e.setCreatedBy(d.createdBy());
        e.setCreatedAt(d.createdAt());
        return e;
    }
}
