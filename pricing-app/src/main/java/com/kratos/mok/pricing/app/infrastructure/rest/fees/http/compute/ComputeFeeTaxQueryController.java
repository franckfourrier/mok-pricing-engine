package com.kratos.mok.pricing.app.infrastructure.rest.fees.http.compute;

import com.kratos.mok.pricing.fees.application.query.computeFee.ComputeFeeQueryHandler;
import com.kratos.mok.pricing.shared.api.MoneyDto;
import com.kratos.mok.pricing.shared.domain.enums.AccountType;
import com.kratos.mok.pricing.shared.domain.enums.TransactionCode;
import com.kratos.mok.pricing.shared.domain.vo.Money;
import com.kratos.mok.pricing.shared.domain.vo.PricingRequestContext;
import com.kratos.mok.pricing.taxes.application.query.computeTax.ComputeTaxQueryHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/fees")
@Slf4j
public class ComputeFeeTaxQueryController {

  private final ComputeFeeQueryHandler feeService;
  private final ComputeTaxQueryHandler taxService;

  public ComputeFeeTaxQueryController(ComputeFeeQueryHandler feeService, ComputeTaxQueryHandler taxService) {
    this.feeService = feeService;
    this.taxService = taxService;
  }

  @Operation(
      summary = "Compute applicable fee and tax",
      description = "Returns the applicable fee and tax only (no commissions)"
  )
  @GetMapping("/compute")
  public FeeTaxComputeResponse computeFee(
      @RequestParam String transactionCode,
      @RequestParam String amount,
      @RequestParam String currency,
      @RequestParam String accountId,
      @RequestParam String accountType,
      @RequestParam boolean kycValidated,
      @RequestParam(required = false) Integer monthlyTxCount,
      @RequestParam OffsetDateTime occurredAt
  ) {

    // Log de réception de la demande d'estimation/simulation
    log.info("[QUERY-API] Received dry-run fee/tax calculation request. code={}, amount={} {}, accountId={}, accountType={}",
        transactionCode, amount, currency, accountId, accountType);
    TransactionCode txCode = TransactionCode.valueOf(transactionCode.trim().toUpperCase());
    Money txAmount = Money.of(amount, currency);

    PricingRequestContext ctx = new PricingRequestContext(
        txCode,
        txAmount,
        accountId,
        AccountType.valueOf(accountType.trim().toUpperCase()),
        kycValidated,
        monthlyTxCount == null ? 0 : monthlyTxCount,
        occurredAt
    );

    var resultFee = feeService.computeFee(ctx);
    // Log intermédiaire pour tracer la politique de frais appliquée
    log.debug("[QUERY-API] Fee computed successfully. policyId={}, calculatedFee={}",
        resultFee.feePolicyId(), resultFee.fee());

    var resultTax = taxService.computeTax(ctx);
    // Log intermédiaire pour tracer le résultat de la taxe
    log.debug("[QUERY-API] Tax computed successfully. totalTax={}", resultTax.totalTax());

    // Log de fin avec le résultat condensé
    log.info("[QUERY-API] Dry-run evaluation completed. code={}, fee={}, totalTax={}",
        txCode.name(), resultFee.fee(), resultTax.totalTax());
    return new FeeTaxComputeResponse(
        txCode.name(),
        new MoneyDto(txAmount.amount(), txAmount.currency()),
        new MoneyDto(resultFee.fee().amount(), resultFee.fee().currency()),
        resultFee.feePolicyId(),
        new MoneyDto(
            resultTax.totalTax().amount(),
            resultTax.totalTax().currency()
        ),
        null
    );
  }
}
