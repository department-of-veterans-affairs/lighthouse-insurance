package gov.va.api.lighthouse.insurance.tests;

import gov.va.api.health.sentinel.ServiceDefinition;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SystemDefinition {

  @NonNull ServiceDefinition insurance;

  @NonNull TestIds testIds;
}
