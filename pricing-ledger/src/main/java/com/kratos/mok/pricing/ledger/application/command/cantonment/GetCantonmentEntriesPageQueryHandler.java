package com.kratos.mok.pricing.ledger.application.command.cantonment;

import com.kratos.mok.pricing.ledger.infrastructure.model.LedgerEntryEntity;
import com.kratos.mok.pricing.ledger.infrastructure.repository.JpaLedgerEntryRepository;
import com.kratos.mok.pricing.shared.api.PageResponseDto;
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
public class GetCantonmentEntriesPageQueryHandler {

    private final JpaLedgerEntryRepository jpaRepository;

    @Value("${ledger.accounts.cantonment}")
    private String accCant;

    public PageResponseDto<CantonmentEntrySummary> handle(GetCantonmentEntriesPageQuery q) {
        int page = Math.max(q.page(), 0);
        int size = Math.min(Math.max(q.size(), 1), 100);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        // 3. Filtrage spécifique au compte de Cantonnement
        Page<LedgerEntryEntity> p = jpaRepository.findByAccountCode(accCant, pageable);

        // 4. Mapping propre vers le format "Entrée/Sortie" du tableau
        Page<CantonmentEntrySummary> mapped = p.map(this::toSummary);

        return PageResponseDto.from(mapped);
    }

    private CantonmentEntrySummary toSummary(LedgerEntryEntity e) {
        boolean isCredit = "CREDIT".equals(e.getDirection().name());
        String amountStr = String.format("%,d FCFA", e.getAmount().longValue());

        return new CantonmentEntrySummary(
                e.getId(),
                e.getId().substring(0, 4).toUpperCase(), // Le "ID" court du tableau
                isCredit ? "+ " + amountStr : "/",        // Colonne Entrée
                !isCredit ? "- " + amountStr : "/",       // Colonne Sortie
                e.getDescription(),                        // Motif
                e.getOccurredAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                e.getOccurredAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
    }
}