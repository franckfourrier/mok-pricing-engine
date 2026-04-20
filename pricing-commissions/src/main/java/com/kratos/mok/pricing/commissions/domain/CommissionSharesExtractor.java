package com.kratos.mok.pricing.commissions.domain;

import com.kratos.mok.pricing.commissions.domain.strategy.*;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionShare;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

final class CommissionSharesExtractor {

    private CommissionSharesExtractor() {}

    static Map<String, BigDecimal> extract(CommissionStrategy strategy) {
        Map<String, BigDecimal> m = new HashMap<>();

        if (strategy instanceof SubscriberDepositStrategy s) {
            putKeys(m, s.keys());

        } else if (strategy instanceof DirectStrategy s) {
            putKeys(m, s.keys());

        } else if (strategy instanceof SubscriberWithdrawalStrategy s) {
            /*m.put("agent", s.agentShare().value());
            m.put("coverage", s.coverageRate().value());
            m.put("kratos", s.kratosShare().value());*/
            putKeys(m, s.keys());
        }

        return Map.copyOf(m);
    }

    private static void putKeys(Map<String, BigDecimal> m, java.util.List<CommissionShare> keys) {
        for (var k : keys) {
            m.put(k.beneficiaryType().name().toLowerCase(), k.share().value());
        }
    }
}
