package com.forsoftwaredevelopers.audio_stream_api.infraestructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.forsoftwaredevelopers.audio_stream_api.domain.model.TokenPayload;
import com.forsoftwaredevelopers.audio_stream_api.domain.result.Result;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.security.JwtService;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        throw new IllegalArgumentException("Missing or invalid Authorization header");
                    }

                    String token = authHeader.substring(7);
                    Result<TokenPayload> validationResult = jwtService.validateToken(token);

                    if (validationResult.isFail()) {
                        throw new IllegalArgumentException("Invalid or expired JWT token");
                    }

                    TokenPayload payload = validationResult.getOrThrow();

                    if (!payload.scopes().contains("socket_listen")) {
                        throw new IllegalArgumentException("Token does not have socket_listen scope");
                    }

                    List<SimpleGrantedAuthority> authorities = payload.scopes().stream()
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .toList();
                    authorities.add(new SimpleGrantedAuthority("TYPE_" + payload.type()));

                    Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            payload.sub(), null, authorities);
                    accessor.setUser(auth);
                }

                return message;
            }
        });
    }
}