package gov.va.api.lighthouse.insurance.tests;

import gov.va.api.health.sentinel.Environment;
import gov.va.api.health.sentinel.SentinelProperties;
import gov.va.api.health.sentinel.ServiceDefinition;
import java.util.Optional;
import lombok.experimental.UtilityClass;

import static gov.va.api.health.sentinel.SentinelProperties.magicAccessToken;

@UtilityClass
class SystemDefinitions {
  private static SystemDefinition lab() {
    String url = "https://sandbox-api.va.gov";
    return SystemDefinition.builder()
        .insurance(serviceDefinition("insurance", url, 443, magicAccessToken(), "/services/fhir/v0/r4/"))
        .testIds(testIds())
        .build();
  }

  private static SystemDefinition local() {
    String url = "http://localhost";
    return SystemDefinition.builder()
        .insurance(serviceDefinition("insurance", url, 8065, null, "/r4/"))
        .testIds(testIds())
        .build();
  }

  private static SystemDefinition qa() {
    String url = "https://blue.qa.lighthouse.va.gov";
    return SystemDefinition.builder()
        .insurance(serviceDefinition("insurance", url, 443, magicAccessToken(), "/fhir/v0/r4/"))
        .testIds(testIds())
        .build();
  }

  private static ServiceDefinition serviceDefinition(
      String name, String url, int port, String accessToken, String apiPath) {
    return SentinelProperties.forName(name)
        .accessToken(() -> Optional.ofNullable(accessToken))
            .defaultUrl(url)
        .defaultPort(port)
        .defaultApiPath(apiPath)
        .defaultUrl(url)
        .build()
        .serviceDefinition();
  }

  private static SystemDefinition stagingLab() {
    String url = "https://blue.staging-lab.lighthouse.va.gov";
    return SystemDefinition.builder()
        .insurance(serviceDefinition("insurance", url, 443, magicAccessToken(), "/fhir/v0/r4/"))
        .testIds(testIds())
        .build();
  }

  static SystemDefinition systemDefinition() {
    switch (Environment.get()) {
      case LOCAL:
        return local();
      case QA:
        return qa();
      case STAGING_LAB:
        return stagingLab();
      case LAB:
        return lab();
      default:
        throw new IllegalArgumentException(
            "Unsupported sentinel environment: " + Environment.get());
    }
  }

  private static TestIds testIds() {
    return TestIds.builder()
        .coverage("I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289")
        .patient("1092387456V321456")
        // Frankenpatient
        .oauthPatient("1017283180V801730")
        .build();
  }
}
