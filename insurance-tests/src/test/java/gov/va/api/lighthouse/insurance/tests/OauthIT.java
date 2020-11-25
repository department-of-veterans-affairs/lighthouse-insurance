package gov.va.api.lighthouse.insurance.tests;

import static gov.va.api.health.sentinel.EnvironmentAssumptions.assumeEnvironmentIn;
import static org.junit.jupiter.api.Assertions.fail;

import gov.va.api.health.r4.api.resources.Coverage;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.ExpectedResponse;
import gov.va.api.health.sentinel.OauthRobotProperties;
import gov.va.api.health.sentinel.ServiceDefinition;
import gov.va.api.health.sentinel.SystemOauthRobot;
import gov.va.api.health.sentinel.TokenExchange;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OauthIT {
  ServiceDefinition def = SystemDefinitions.systemDefinition().insurance();

  TestIds ids = SystemDefinitions.systemDefinition().testIds();

  private String exchangeToken() {
    SystemOauthRobot.Configuration config =
        OauthRobotProperties.usingSystemProperties()
            .forSystemOauth()
            // Loading ClientId, ClientSecret, Audience, and TokenUrl will be done via properties
            .defaultScopes(List.of("system/Coverage.read"))
            .build()
            .systemOauthConfig();
    TokenExchange token = SystemOauthRobot.builder().config(config).build().token();
    if (token.isError()) {
      fail("Failed to get token result from oauth robot.");
    }
    return token.accessToken();
  }

  @Test
  void oauthFlow() {
    assumeEnvironmentIn(Environment.STAGING_LAB, Environment.LAB);
    String token = exchangeToken();
    // Valid Token
    test(200, Coverage.Bundle.class, token, "Coverage?patient={icn}", ids.oauthPatient());
    // Invalid Token
    test(401, OperationOutcome.class, "NOPE", "Coverage?patient={icn}", ids.oauthPatient());
    // Invalid Resource for Scopes
    // Need a second fhir path on the green load-balancer to test this
    // test(403, OperationOutcome.class, token, "Observation?patient={icn}", ids.oauthPatient());
  }

  private void test(int status, Class<?> expected, String token, String path, String... params) {
    var requestPath = def.apiPath() + path;
    log.info(
        "Oauth: Expect {} ({}) for {} {}",
        expected.getSimpleName(),
        status,
        requestPath,
        Arrays.toString(params));
    // We have to rebuild the request spec.
    // Kong does not like multiple auth headers.
    // This prevents two Authorization headers from resulting in a 400.
    RequestSpecification request =
        RestAssured.given()
            .baseUri(def.url())
            .port(def.port())
            .relaxedHTTPSValidation()
            .headers(Map.of("Authorization", "Bearer " + token))
            .contentType("application/json")
            .accept("application/json");
    ExpectedResponse response =
        ExpectedResponse.of(request.request(Method.GET, requestPath, params));

    response.expect(status).expectValid(expected);
  }
}
