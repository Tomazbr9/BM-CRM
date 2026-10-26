package com.bmtechnology.bmcrm;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularArchitectureTest {

    @Test
    void shouldRespectModuleBoundaries() {

        ApplicationModules modules =
                ApplicationModules.of(BuildpriceApplication.class);

        modules.verify();
    }
}
