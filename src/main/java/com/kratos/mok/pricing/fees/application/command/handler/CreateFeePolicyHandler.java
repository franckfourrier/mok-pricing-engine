package com.kratos.mok.pricing.fees.application.command.handler;

import com.kratos.mok.pricing.fees.application.command.CreateFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.CreateFeePolicyResponse;
import com.kratos.mok.pricing.fees.domain.FeePolicy;
import com.kratos.mok.pricing.fees.domain.repository.FeePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateFeePolicyHandler {

    private final FeePolicyRepository repository;

    public CreateFeePolicyHandler(FeePolicyRepository repository) {
        this.repository = repository;
    }

    public CreateFeePolicyResponse handle(CreateFeePolicyCommand cmd) {
        FeePolicy policy = FeePolicy.create(
                cmd.type(), cmd.target(), cmd.strategy(),
                cmd.limits(), cmd.activationThreshold(), cmd.validity(),
                cmd.kycRequired(), cmd.authorId()
        );

        repository.save(policy);
        return new CreateFeePolicyResponse(policy.snapshot().id(), true);
    }
}
