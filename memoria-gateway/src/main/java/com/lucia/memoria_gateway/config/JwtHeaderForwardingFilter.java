package com.lucia.memoria_gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtHeaderForwardingFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> {
          var auth = context.getAuthentication();
          if(auth!=null && auth.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getSubject();
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId)
                .build();
            return exchange.mutate().request(mutatedRequest).build();
          }
          return exchange;
        })
        .defaultIfEmpty(exchange)
        .flatMap(chain::filter);
  }
}
