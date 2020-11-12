package gov.va.api.lighthouse.insurance.tests;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TestIds {
  @NonNull String coverage;
  @NonNull String patient;
}
