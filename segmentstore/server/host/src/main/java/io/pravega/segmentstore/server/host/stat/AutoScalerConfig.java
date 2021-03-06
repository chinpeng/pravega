/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package io.pravega.segmentstore.server.host.stat;

import io.pravega.common.util.ConfigBuilder;
import io.pravega.common.util.ConfigurationException;
import io.pravega.common.util.Property;
import io.pravega.common.util.TypedProperties;
import java.net.URI;
import java.time.Duration;
import lombok.Data;
import lombok.Getter;

@Data
public class AutoScalerConfig {
    public static final Property<String> REQUEST_STREAM = Property.named("requestStream", "_requeststream");
    public static final Property<Integer> COOLDOWN_IN_SECONDS = Property.named("cooldownInSeconds", 10 * 60);
    public static final Property<Integer> MUTE_IN_SECONDS = Property.named("muteInSeconds", 10 * 60);
    public static final Property<Integer> CACHE_CLEANUP_IN_SECONDS = Property.named("cacheCleanUpInSeconds", 5 * 60);
    public static final Property<Integer> CACHE_EXPIRY_IN_SECONDS = Property.named("cacheExpiryInSeconds", 20 * 60);
    public static final Property<String> CONTROLLER_URI = Property.named("controllerUri", "tcp://localhost:9090");
    public static final Property<Boolean> TLS_ENABLED = Property.named("tlsEnabled", false);
    public static final Property<String> TLS_CERT_FILE = Property.named("tlsCertFile", "");
    public static final Property<Boolean> AUTH_ENABLED = Property.named("authEnabled", false);
    public static final Property<String> TOKEN_SIGNING_KEY = Property.named("tokenSigningKey", "secret");

    public static final String COMPONENT_CODE = "autoScale";

    /**
     * Uri for controller.
     */
    @Getter
    private final URI controllerUri;
    /**
     * Stream on which scale requests have to be posted.
     */
    @Getter
    private final String internalRequestStream;
    /**
     * Duration for which no scale operation is attempted on a segment after its creation.
     */
    @Getter
    private final Duration cooldownDuration;
    /**
     * Duration for which scale requests for a segment are to be muted.
     * Mute duration is per request type (scale up and scale down). It means if a scale down request was posted
     * for a segment, we will wait until the mute duration before posting the request for the same segment
     * again in the request stream.
     */
    @Getter
    private final Duration muteDuration;
    /**
     * Duration for which a segment lives in auto scaler cache, after which it is expired and a scale down request with
     * silent flag is sent for the segment.
     */
    @Getter
    private final Duration cacheExpiry;
    /**
     * Periodic time period for scheduling auto-scaler cache clean up. Since guava cache does not maintain its own executor,
     * we need to keep performing periodic cache maintenance activities otherwise caller's thread using the cache will be used
     * for cache maintenance.
     * This also ensures that if there is no traffic in the cluster, all segments that have expired are cleaned up from the cache
     * and their respective removal code is invoked.
     */
    @Getter
    private final Duration cacheCleanup;

    /**
     * Flag to represent the case where interactions with controller are encrypted with TLS.
     */
    @Getter
    private final boolean tlsEnabled;

    /**
     * The X.509 certificate file used for TLS connection to controller.
     */
    @Getter
    private final String tlsCertFile;

    /**
     * Flag to represent the case where controller expects authorization details.
     */
    @Getter
    private final boolean authEnabled;

    /**
     *
     */
    @Getter
    private final String tokenSigningKey;

    private AutoScalerConfig(TypedProperties properties) throws ConfigurationException {
        this.internalRequestStream = properties.get(REQUEST_STREAM);
        this.cooldownDuration = Duration.ofSeconds(properties.getInt(COOLDOWN_IN_SECONDS));
        this.muteDuration = Duration.ofSeconds(properties.getInt(MUTE_IN_SECONDS));
        this.cacheCleanup = Duration.ofSeconds(properties.getInt(CACHE_CLEANUP_IN_SECONDS));
        this.cacheExpiry = Duration.ofSeconds(properties.getInt(CACHE_EXPIRY_IN_SECONDS));
        this.controllerUri = URI.create(properties.get(CONTROLLER_URI));
        this.tlsEnabled = properties.getBoolean(TLS_ENABLED);
        this.authEnabled = properties.getBoolean(AUTH_ENABLED);
        this.tlsCertFile = properties.get(TLS_CERT_FILE);
        this.tokenSigningKey = properties.get(TOKEN_SIGNING_KEY);
    }

    public static ConfigBuilder<AutoScalerConfig> builder() {
        return new ConfigBuilder<>(COMPONENT_CODE, AutoScalerConfig::new);
    }
}
