package com.tlcn.sportsnet_backend.config;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;
import java.util.List;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final AccountRepository accountRepository;

    @Autowired
    public CustomJwtAuthenticationConverter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getSubject();
        if (email == null) {
            throw new DisabledException("Token không hợp lệ (thiếu subject)");
        }

        List<String> authorities = jwt.getClaimAsStringList("authorities");
        if (authorities == null) authorities = Collections.emptyList();

        Account user = accountRepository.findByEmail(email)
                .orElseThrow(() -> new DisabledException("Tài khoản không tồn tại"));

        if (!user.isEnabled()) {
            throw new DisabledException("Tài khoản đã bị khóa");
        }
        System.out.println("Check qua tài khoản");
        if(!user.isVerified()){
            throw new DisabledException("Tài khoản chưa được xác thực");
        }
        // Nếu bạn có thêm field `banned`
        // if (user.isBanned()) throw new DisabledException("Tài khoản đã bị cấm");

        return new JwtAuthenticationToken(
                jwt,
                authorities.stream().map(SimpleGrantedAuthority::new).toList(),
                email
        );
    }
}
