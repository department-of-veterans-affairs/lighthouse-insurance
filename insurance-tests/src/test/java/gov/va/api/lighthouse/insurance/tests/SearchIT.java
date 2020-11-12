package gov.va.api.lighthouse.insurance.tests;

import static gov.va.api.lighthouse.insurance.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.lighthouse.insurance.tests.TestClients.r4Scheduling;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.TestClient;
import org.junit.jupiter.api.Test;

public class SearchIT {
  public void checkResponse(String endpoint, Class expectedClass) {
    TestClient ftc = r4Scheduling();
    String apiPath = ftc.service().urlWithApiPath();
    ExpectedResponse response = ftc.get(apiPath + endpoint);
    response.expect(200).expectValid(expectedClass);
    assertThat(response.response()).isNotNull();
  }

  @Test
  public void searchByCoverageIdTest() {
    final String coverageId = systemDefinition().testIds().coverage();
    checkResponse("Coverage/" + coverageId, Coverage.class);
  }

  @Test
  public void searchByParametersTest() {
    final String coverageId = systemDefinition().testIds().coverage();
    final String patientId = systemDefinition().testIds().patient();
    checkResponse("Coverage?_id=" + coverageId, Coverage.Bundle.class);
    checkResponse("Coverage?identifier=" + coverageId, Coverage.Bundle.class);
    checkResponse("Coverage?patient=" + patientId, Coverage.Bundle.class);
  }
}
