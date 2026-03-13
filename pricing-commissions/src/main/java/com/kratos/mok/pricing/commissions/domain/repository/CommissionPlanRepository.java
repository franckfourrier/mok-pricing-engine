package com.kratos.mok.pricing.commissions.domain.repository;

import com.kratos.mok.pricing.commissions.domain.CommissionPlan;
import com.kratos.mok.pricing.commissions.domain.vo.CommissionPlanId;
import com.kratos.mok.pricing.shared.domain.enums.TargetScope;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.enums.TransactionType;

import java.util.List;
import java.util.Optional;

public interface CommissionPlanRepository {
    void save(CommissionPlan plan);

    Optional<CommissionPlan> findById(CommissionPlanId id);

    List<CommissionPlan> findCandidates(TransactionType type, String accountType, String accountId);

    List<CommissionPlan> findCandidates(TransactionCode transactionCode, String accountType, String accountId);

    boolean existsConflictingPlan(CommissionPlan plan);

    boolean existsAnyFor(TransactionCode transactionCode, TargetScope scope, String value);}
