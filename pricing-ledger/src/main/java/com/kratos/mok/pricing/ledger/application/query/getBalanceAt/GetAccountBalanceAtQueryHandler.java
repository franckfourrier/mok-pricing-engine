package com.kratos.mok.pricing.ledger.application.query.getBalanceAt;

import com.kratos.mok.pricing.ledger.domain.repository.LedgerAccountRepository;
import com.kratos.mok.pricing.ledger.domain.repository.LedgerEntryRepository;
import com.kratos.mok.pricing.ledger.domain.vo.AccountCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAccountBalanceAtQueryHandler {

    private final LedgerAccountRepository accounts;
    private final LedgerEntryRepository entries;

    @Transactional(readOnly = true)
    public AccountBalanceView handle(GetAccountBalanceAtQuery q) {
        if (q.accountCode() == null || q.accountCode().isBlank()) throw new IllegalArgumentException("accountCode is required");
        if (q.at() == null) throw new IllegalArgumentException("at is required");

        AccountCode code = AccountCode.of(q.accountCode());
        var acc = accounts.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Unknown ledger account: " + code.value()));

        Money bal = entries.balanceAt(code, q.at(), acc.currency());
        return new AccountBalanceView(code.value(), bal, 0,acc.currency());
    }
}
