package com.kratos.mok.pricing.app.infrastructure.http;

import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyCommand;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyHandler;
import com.kratos.mok.pricing.fees.application.command.createFeePolicy.CreateFeePolicyResponse;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/fee-policies")
@RequiredArgsConstructor
public class FeePolicyController {

    private final CreateFeePolicyHandler handler;

    public FeePolicyController(CreateFeePolicyHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CreateFeePolicyResponse> create(
            @RequestBody @Valid CreateFeePolicyCommand cmd,
            Authentication auth
    ) {
        String authorId = auth.getName(); // ou claim "sub"
        var res = handler.handle(cmd, authorId);

        // Si tu renvoies 201 systématiquement :
        return ResponseEntity
                .created(URI.create("/api/fee-policies/" + res.policyId()))
                .body(res);
    }
}
