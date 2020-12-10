package gov.va.api.lighthouse.insurance.tests;

import static gov.va.api.lighthouse.insurance.tests.SystemDefinitions.systemDefinition;
import static gov.va.api.lighthouse.insurance.tests.TestClients.r4Scheduling;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.TestClient;
import org.junit.jupiter.api.Test;

public class CreateIT {

  public void checkResponse(String endpoint, String body, int expected) {
    TestClient ftc = r4Scheduling();
    String apiPath = ftc.service().urlWithApiPath();
    ExpectedResponse response = ftc.post(apiPath + endpoint, body);
    response.expect(expected);
    assertThat(response.response()).isNotNull();
  }

  @Test
  public void postCoverageTest() {
    final String coverageId = systemDefinition().testIds().coverage();
    checkResponse(
        "Coverage",
        "{\"id\":\"I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289\",\"resourceType\":\"Coverage\"}",
        201);
    checkResponse("Coverage", "", 400);
    checkResponse("Coverage", "{\"id\":\"I2-500\",\"resourceType\":\"Coverage\"}", 500);
  }
}
