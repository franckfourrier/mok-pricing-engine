package com.kratos.mok.pricing.app.infrastructure.logging;

import com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute.FeeTaxComputeResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PricingRestApiAuditAspect {

  // =====================================================
  // POINTCUTS
  // =====================================================

  @Pointcut("execution(* com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute.ComputeFeeTaxQueryController.computeFee(..))")
  public void feeTaxSimulationExecution() {
  }

  // =====================================================
  // SIMULATION ORCHESTRATION (START, SUCCESS, DURATION)
  // =====================================================

  @Around("feeTaxSimulationExecution()")
  public Object monitorSimulationPipeline(ProceedingJoinPoint pjp) throws Throwable {
    Object[] args = pjp.getArgs();

    // Extraction positionnelle et sécurisée des RequestParams HTTP
    String txCode = safeStringArg(args, 0);
    String amount = safeStringArg(args, 1);
    String currency = safeStringArg(args, 2);
    String accountId = safeStringArg(args, 3);
    String accountType = safeStringArg(args, 4);

    log.info("[PRICING-API][SIMULATION-START] code={} | amount={} {} | account={} ({})",
        txCode, amount, currency, accountId, accountType);

    long start = System.currentTimeMillis();

    try {
      Object result = pjp.proceed();
      long duration = System.currentTimeMillis() - start;

      if (result instanceof FeeTaxComputeResponse response) {
        String feeStr = response.fee() != null ? response.fee().amount().toString() + " " + response.fee().currency() : "0.0";
        String taxStr = response.tax() != null ? response.tax().amount().toString() + " " + response.tax().currency() : "0.0";

        log.info("[PRICING-API][SIMULATION-SUCCESS] code={} | fee={} [policy={}] | tax={} | duration={} ms",
            response.transactionCode(),
            feeStr,
            safe(response.feePolicyId()),
            taxStr,
            duration);
      }

      return result;

    } catch (Throwable ex) {
      long duration = System.currentTimeMillis() - start;
      log.error("[PRICING-API][SIMULATION-FAILED] code={} | accountId={} | duration={} ms | error={}",
          txCode, accountId, duration, ex.getMessage());
      throw ex;
    }
  }

  // =====================================================
  // REST API ERRORS
  // =====================================================

  @AfterThrowing(pointcut = "feeTaxSimulationExecution()", throwing = "exception")
  public void logRestError(JoinPoint joinPoint, Throwable exception) {
    log.error("[PRICING-API][ERROR] class={} | method={} | message={}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
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

  private String safeStringArg(Object[] args, int index) {
    if (args != null && args.length > index && args[index] != null) {
      return args[index].toString();
    }
    return "UNKNOWN";
  }
}
