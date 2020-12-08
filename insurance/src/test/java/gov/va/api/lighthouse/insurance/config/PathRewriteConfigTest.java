package gov.va.api.lighthouse.insurance.config;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.r4.api.resources.Coverage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PathRewriteConfigTest {
  @Autowired TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  void pathIsRewritten() {
    assertThat(
        restTemplate.getForObject(
            "http://localhost:" + port + "/insurance/r4/Coverage/123", Coverage.class));
    assertThat(
        restTemplate.getForObject(
            "http://localhost:" + port + "/services/fhir/v0/r4/Coverage/123", Coverage.class));
    assertThat(
        restTemplate.getForObject(
            "http://localhost:" + port + "/fhir/v0/r4/Coverage/123", Coverage.class));
  }
}
