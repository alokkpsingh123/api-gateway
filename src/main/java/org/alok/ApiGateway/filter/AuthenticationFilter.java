package org.alok.ApiGateway.filter;

import org.alok.ApiGateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class  AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config>{

//    @Autowired
//    private RestTemplate template;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private RouteValidator validator;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            ServerHttpRequest request = null;

            if(validator.isSecured.test(exchange.getRequest())){
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                    throw new RuntimeException("missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if(authHeader != null && authHeader.startsWith("Bearer ")){
                    authHeader = authHeader.substring(7);
                }
                try{
                    //REST call to AUTH service
                    //template.getForObject("http://AUTH-SERVICE/validate-token?token"+authHeader, String.class);
                    jwtUtil.validateToken(authHeader);

                    request = exchange.getRequest()
                            .mutate()
                            .header("loggedInUser", jwtUtil.extractUsername(authHeader))
//                            .header("loggedInEmail", jwtUtil.extractEmail(authHeader))
                            .build();


                }catch (Exception e){
                    System.out.println("invalid access...!");
                    throw new RuntimeException("un-authorized access to application");
                }
            }

            return  chain.filter(exchange.mutate().request(request).build());
        });
    }


    public static class Config{

    }
}
