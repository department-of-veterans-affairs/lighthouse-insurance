package gov.va.api.lighthouse.insurance.tests;

import static gov.va.api.lighthouse.insurance.tests.SystemDefinitions.systemDefinition;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.sentinel.FhirTestClient;
import gov.va.api.health.sentinel.TestClient;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestClients {
  TestClient r4Scheduling() {
    return FhirTestClient.builder()
        .service(systemDefinition().insurance())
        .contentTypes(List.of("application/json", "application/fhir+json"))
        .mapper(JacksonConfig::createMapper)
        .errorResponseEqualityCheck(
            new gov.va.api.lighthouse.insurance.tests.OperationOutcomesAreFunctionallyEqual())
        .build();
  }
}
