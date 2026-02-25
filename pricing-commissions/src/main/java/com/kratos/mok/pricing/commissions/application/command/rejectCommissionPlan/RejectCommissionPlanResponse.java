package com.kratos.mok.pricing.commissions.application.command.rejectCommissionPlan;

public record RejectCommissionPlanResponse(String planId, boolean success, String status) {}
