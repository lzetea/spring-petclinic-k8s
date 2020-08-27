/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.customers.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application metrics
 *
 * @author Alexandre Roman
 */
@Configuration
public class AppMetrics {
    @Bean
    Counter appVersion(MeterRegistry reg, BuildProperties build) {
        final Counter counter = Counter.builder("petclinic.version")
            .description("Application version")
            .tags("application", build.getName(), "version", build.getVersion())
            .register(reg);
        counter.increment();
        return counter;
    }
}
