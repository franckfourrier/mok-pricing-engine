package com.kratos.mok.pricing.ledger.application.query.cantonment;

import com.kratos.mok.pricing.ledger.application.query.dashboard.GetDashboardCachedQueryHandler;
import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerEntryEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.JpaLedgerEntryRepository;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
import com.kratos.mok.pricing.shared.domain.money.MoneyFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GetCantonmentEntriesPageQueryHandler { //TODO

    private final JpaLedgerEntryRepository jpaRepository;
    private final GetDashboardCachedQueryHandler cachedDashboardHandler;

    @Value("${ledger.accounts.cantonment}")
    private String accCant;

    public CantonmentDashboardResponse handle(GetCantonmentEntriesPageQuery q) {
        // 1. Récupération du solde via le cache (Snapshot)
        var dashboard = cachedDashboardHandler.handle();
        var cantBalance = dashboard.cant();

        // 2. Pagination
        int page = Math.max(q.page(), 0);
        int size = Math.min(Math.max(q.size(), 1), 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        Page<LedgerEntryEntity> p = jpaRepository.findByAccountCode(accCant, pageable);
        var history = PageResponseDto.from(p.map(this::toSummary));

        // 3. Formatage dynamique via MoneyFormatter (Gère les centimes et XAF -> FCFA)
        String formattedBalance = MoneyFormatter.format(cantBalance.amount(), cantBalance.currency());

        return new CantonmentDashboardResponse(
                cantBalance.amount(),
                formattedBalance,
                cantBalance.currency(),
                cantBalance.trend(),
                history
        );
    }

    private CantonmentEntrySummary toSummary(LedgerEntryEntity e) {
        boolean isCredit = "CREDIT".equals(e.getDirection().name());

        // On utilise l'utilitaire partagé ici aussi pour garder les décimales (%,.2f)
        String amountStr = MoneyFormatter.format(e.getAmount(), e.getCurrency());

        return new CantonmentEntrySummary(
                e.getId(),
                e.getId().substring(0, 4).toUpperCase(),
                isCredit ? "+ " + amountStr : "/",
                !isCredit ? "- " + amountStr : "/",
                e.getDescription(),
                e.getOccurredAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                e.getOccurredAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
    }
}