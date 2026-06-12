package com.kratos.mok.pricing.app.infrastructure.logging.pricing;

import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionCommand;
import com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionResponse;
import com.kratos.mok.pricing.commissions.application.port.ComputeCommissionDistributionQuery.CommissionDistributionResult;
import com.kratos.mok.pricing.fees.application.port.FeeComputationResult;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.port.TaxComputationResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class PricingAuditAspect {

  // =====================================================
  // POINTCUTS
  // =====================================================

  @Pointcut("execution(* com.kratos.mok.pricing.app.application.command.applyPricingToTransaction.ApplyPricingToTransactionCommandHandler.handle(..))")
  public void pricingExecution() {
  }

  @Pointcut("execution(* com.kratos.mok.pricing.commissions.application.query.ComputeCommissionDistributionQueryImpl.compute(..))")
  public void commissionExecution() {
  }

  @Pointcut("execution(* com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler.computeFee(..))")
  public void feeExecution() {
  }

  @Pointcut("execution(* com.kratos.mok.pricing.taxes.application.query.computeTax.ComputeTaxQueryHandler.computeTax(..))")
  public void taxExecution() {
  }

  @Pointcut("execution(* com.kratos.mok.pricing.ledger.application.port.LedgerWriter.record(..))")
  public void ledgerExecution() {
  }

  // =====================================================
  // PRICING ORCHESTRATION
  // =====================================================

  @Around("pricingExecution() && args(cmd, actor)")
  public Object monitorPricingPipeline(
      ProceedingJoinPoint pjp,
      ApplyPricingToTransactionCommand cmd,
      String actor
  ) throws Throwable {

    log.info(
        "[PRICING][START] txId={} | code={} | amount={} {} | account={} ({}) | kyc={} | monthlyTx={} | actor={}",
        cmd.externalTxId(),
        cmd.transactionCode(),
        safe(cmd.amount()),
        cmd.currency(),
        cmd.accountId(),
        cmd.accountType(),
        cmd.kycValidated(),
        cmd.monthlyTxCount(),
        safeActor(actor)
    );

    if (log.isDebugEnabled()) {
      log.debug(
          "[PRICING][HIERARCHY] txId={} | agent={} | distributor={} | superDistributor={}",
          cmd.externalTxId(),
          cmd.accountId(),
          cmd.distributorAccountId(),
          cmd.superDistributorAccountId()
      );
    }

    long start = System.currentTimeMillis();

    try {

      Object result = pjp.proceed();

      long duration = System.currentTimeMillis() - start;

      if (result instanceof ApplyPricingToTransactionResponse response) {

        log.info(
            "[PRICING][SUCCESS] txId={} | recorded={} | duration={} ms",
            response.externalTxId(),
            response.recorded(),
            duration
        );

        log.info(
            "[PRICING][FINANCIAL] fee={} | tax={} | totalDeducted={} | totalCommission={}",
            response.serviceFee(),
            response.taxAmount(),
            response.totalDeducted(),
            response.totalCommissionOut()
        );

        if (response.payouts() != null && !response.payouts().isEmpty()) {

          String payouts = response.payouts()
              .stream()
              .map(p -> String.format(
                  "%s(%s)=%s",
                  p.beneficiary(),
                  p.accountId(),
                  p.amount()))
              .collect(Collectors.joining(" | "));

          log.info(
              "[PRICING][PAYOUTS] txId={} | count={} | {}",
              response.externalTxId(),
              response.payouts().size(),
              payouts
          );
        }
      }

      return result;

    } catch (Throwable ex) {

      long duration = System.currentTimeMillis() - start;

      log.error(
          "[PRICING][FAILED] txId={} | method={} | duration={} ms",
          cmd.externalTxId(),
          pjp.getSignature().getName(),
          duration
      );

      throw ex;
    }
  }

  // =====================================================
  // COMMISSION
  // =====================================================

  @Before("commissionExecution() && args(ctx, commissionBase)")
  public void logCommissionStart(
      PricingRequestContext ctx,
      Money commissionBase
  ) {

    log.info(
        "[COMMISSION][START] txCode={} | accountId={} | base={}",
        ctx.transactionCode(),
        ctx.accountId(),
        commissionBase
    );
  }

  @AfterReturning(pointcut = "commissionExecution()", returning = "result")
  public void logCommissionSuccess(
      CommissionDistributionResult result
  ) {

    log.info(
        "[COMMISSION][SUCCESS] planId={} | lines={}",
        result.commissionPlanId(),
        result.lines().size()
    );

    if (log.isDebugEnabled()) {

      String details = result.lines()
          .stream()
          .map(l -> l.beneficiary() + "=" + l.amount())
          .collect(Collectors.joining(" | "));

      log.debug("[COMMISSION][DETAILS] {}", details);
    }
  }

  // =====================================================
  // FEES
  // =====================================================

  @Before("feeExecution()")
  public void logFeeStart() {
    log.debug("[FEE][START]");
  }

  @AfterReturning(pointcut = "feeExecution()", returning = "result")
  public void logFeeSuccess(
      FeeComputationResult result
  ) {

    log.debug(
        "[FEE][SUCCESS] policy={} | amount={}",
        result.feePolicyId(),
        result.fee()
    );
  }

  // =====================================================
  // TAX
  // =====================================================

  @Before("taxExecution()")
  public void logTaxStart() {
    log.debug("[TAX][START]");
  }

  @AfterReturning(pointcut = "taxExecution()", returning = "result")
  public void logTaxSuccess(
      TaxComputationResult result
  ) {

    log.debug(
        "[TAX][SUCCESS] total={} | policies={}",
        result.totalTax(),
        result.lines().size()
    );
  }

  // =====================================================
  // LEDGER
  // =====================================================

  @Before("ledgerExecution()")
  public void logLedgerStart() {

    log.info(
        "[LEDGER][START] recording entries"
    );
  }

  @AfterReturning("ledgerExecution()")
  public void logLedgerSuccess() {

    log.info(
        "[LEDGER][SUCCESS] entries recorded"
    );
  }

  // =====================================================
  // ERRORS
  // =====================================================

  @AfterThrowing(
      pointcut = "pricingExecution() || commissionExecution() || feeExecution() || taxExecution()",
      throwing = "exception"
  )
  public void logError(
      JoinPoint joinPoint,
      Throwable exception
  ) {

    log.error(
        "[ERROR] class={} | method={} | txId={} | message={}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        extractTxId(joinPoint),
        exception.getMessage(),
        exception
    );
  }

  // =====================================================
  // HELPERS
  // =====================================================

  private String safe(Object value) {
    return value == null ? "NULL" : value.toString();
  }

  private String safeActor(String actor) {
    return actor == null || actor.isBlank()
        ? "UNKNOWN"
        : actor;
  }

  private String extractTxId(JoinPoint joinPoint) {

    for (Object arg : joinPoint.getArgs()) {

      if (arg instanceof ApplyPricingToTransactionCommand cmd) {
        return cmd.externalTxId();
      }
    }

    return "UNKNOWN";
  }
}
