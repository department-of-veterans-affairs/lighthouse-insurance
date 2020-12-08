package gov.va.api.lighthouse.insurance.tests;

import static gov.va.api.lighthouse.insurance.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.lighthouse.insurance.tests.TestClients.r4Scheduling;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.TestClient;
import org.junit.jupiter.api.Test;

public class SearchIT {

  public void checkResponse(String endpoint, int expected, Class<?> expectedClass) {
    TestClient ftc = r4Scheduling();
    String apiPath = ftc.service().urlWithApiPath();
    ExpectedResponse response = ftc.get(apiPath + endpoint);
    response.expect(expected).expectValid(expectedClass);
    assertThat(response.response()).isNotNull();
  }

  @Test
  public void searchByCoverageIdTest() {
    final String coverageId = systemDefinition().testIds().coverage();
    checkResponse("Coverage/" + coverageId, 200, Coverage.class);
    checkResponse("Coverage/I2-404NotFound", 404, OperationOutcome.class);
    checkResponse("Coverage/I2-500InternalServerError", 500, OperationOutcome.class);
  }

  @Test
  public void searchByParametersTest() {
    final String coverageId = systemDefinition().testIds().coverage();
    final String patientId = systemDefinition().testIds().patient();
    checkResponse("Coverage?_id=" + coverageId, 200, Coverage.Bundle.class);
    checkResponse("Coverage?identifier=" + coverageId, 200, Coverage.Bundle.class);
    checkResponse("Coverage?patient=" + patientId, 200, Coverage.Bundle.class);
    checkResponse("Coverage?_id=I2-404NotFound", 404, OperationOutcome.class);
    checkResponse("Coverage?identifier=I2-404NotFound", 404, OperationOutcome.class);
    checkResponse("Coverage?patient=I2-404NotFound", 404, OperationOutcome.class);
    checkResponse("Coverage?_id=I2-500InternalServerError", 500, OperationOutcome.class);
    checkResponse("Coverage?identifier=I2-500InternalServerError", 500, OperationOutcome.class);
    checkResponse("Coverage?patient=I2-500InternalServerError", 500, OperationOutcome.class);
  }
}
