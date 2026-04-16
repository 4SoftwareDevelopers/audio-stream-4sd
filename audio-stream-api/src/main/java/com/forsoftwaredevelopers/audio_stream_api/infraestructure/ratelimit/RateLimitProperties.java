package com.forsoftwaredevelopers.audio_stream_api.infraestructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int ipRequestsPerMinute = 100;
    private int tokenRequestsPerMinute = 50;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getIpRequestsPerMinute() { return ipRequestsPerMinute; }
    public void setIpRequestsPerMinute(int ipRequestsPerMinute) { this.ipRequestsPerMinute = ipRequestsPerMinute; }
    public int getTokenRequestsPerMinute() { return tokenRequestsPerMinute; }
    public void setTokenRequestsPerMinute(int tokenRequestsPerMinute) { this.tokenRequestsPerMinute = tokenRequestsPerMinute; }
}