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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.List;

/**
 * Pets metrics.
 *
 * @author Alexandre Roman
 */
@Configuration
@RequiredArgsConstructor
public class PetsMetrics {
    private static final String ANIMALS_UNIT = "animals";
    private final MeterRegistry reg;
    private final PetRepository repo;

    @EventListener
    void registerMetrics(ApplicationReadyEvent e) {
        registerPetsMetrics();
    }

    private void registerPetsMetrics() {
        final List<PetType> petTypes = repo.findPetTypes();
        for (final PetType petType : petTypes) {
            final String petTypeName = repo.findPetTypeById(petType.getId()).get().getName();

            Gauge.builder("petclinic.pets", () -> {
                return repo.countPetsByType(petType);
            }).baseUnit("animals")
                .description("Number of pets")
                .tag("type", petTypeName)
                .register(reg);
        }
    }
}
