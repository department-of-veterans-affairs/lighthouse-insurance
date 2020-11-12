package gov.va.api.lighthouse.insurance;

import gov.va.api.health.r4.api.bundle.AbstractBundle;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.datatypes.CodeableConcept;
import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.datatypes.Money;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Coverage;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    value = "/r4/Coverage",
    produces = {"application/json", "application/fhir+json"})
public class InsuranceController {

  private final String basepath;

  InsuranceController(@Value("${insurance.base-url}") String basepath) {
    this.basepath = basepath;
  }

  @SneakyThrows
  Coverage buildCoverage() {
    String coverageID = "I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289";
    String patientID = "1092387456V321456";
    return Coverage.builder()
        .resourceType("Coverage")
        .id(coverageID)
        .status(Coverage.Status.active)
        .subscriberId("I2-9HRPVUQL0289KNR9KLJTLHA5RY000481")
        .beneficiary(
            Reference.builder()
                .reference(basepath + "r4/Patient/" + patientID)
                .display("JOHN Q VETERAN")
                .build())
        .relationship(CodeableConcept.builder().build())
        .payor(
            List.of(
                Reference.builder()
                    .reference("r4/Organization/I2-1PQBLFRZ6207KNR9KLJTLHA5RY000903")
                    .display("EXH INSURANCE")
                    .build()))
        .coverageClass(
            List.of(
                Coverage.CoverageClass.builder()
                        .id("I2-2PQBLFRZ0319NWO5JRPVQIY1IP000010")
                    .type(
                        CodeableConcept.builder()
                            .coding(List.of(Coding.builder().code("group").build()))
                            .build())
                    .value("GroupValue")
                    .build()))
        .order(1)
        .costToBeneficiary(
            List.of(
                Coverage.CostToBeneficiary.builder()
                    .id("I2-7QTRSBNY6915LWV0KLJTLHA4PL000366")
                    .valueMoney(
                        Money.builder().currency("USD").value(BigDecimal.valueOf(100)).build())
                    .build()))
        .build();
  }

  /** Read coverage by ID. */
  @GetMapping(value = "/{id}")
  Coverage readCoverageId(@PathVariable("id") String id) {
    return buildCoverage();
  }

  /** Get coverage by search parameters. */
  @GetMapping
  Coverage.Bundle readCoverageSearchParameters(
      @RequestParam(value = "_id", required = false) String id,
      @RequestParam(value = "patient", required = false) String patient,
      @RequestParam(value = "identifier", required = false) String identifier) {
    Coverage coverage = buildCoverage();
    var queryString =
        StubbedQueryStringBuilder.builder()
            .id(Optional.ofNullable(id))
            .identifier(Optional.ofNullable(identifier))
            .pat(Optional.ofNullable(patient))
            .build()
            .toStubbedQueryString();
    return Coverage.Bundle.builder()
        .link(
            Arrays.asList(
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.first)
                    .url(basepath + "r4/Coverage?" + queryString + "&page=1&count=1")
                    .build(),
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.self)
                    .url(basepath + "r4/Coverage?" + queryString + "&page=1&count=1")
                    .build(),
                BundleLink.builder()
                    .relation(BundleLink.LinkRelation.last)
                    .url(basepath + "r4/Coverage?" + queryString + "&page=1&count=1")
                    .build()))
        .resourceType("Bundle")
        .type(AbstractBundle.BundleType.searchset)
        .total(1)
        .entry(
            List.of(
                Coverage.Entry.builder()
                    .fullUrl(basepath + "r4/Coverage/I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289")
                    .resource(coverage)
                    .build()))
        .build();
  }

  @Builder
  public static class StubbedQueryStringBuilder {
    private Optional<String> id;
    private Optional<String> pat;
    private Optional<String> identifier;

    /** Builds a query string representation based on the query params provided. */
    public String toStubbedQueryString() {
      StringBuilder queryStringBuilder = new StringBuilder();
      id.ifPresent(s -> queryStringBuilder.append("_id=").append(s).append("&"));
      pat.ifPresent(s -> queryStringBuilder.append("patient=").append(s).append("&"));
      identifier.ifPresent(s -> queryStringBuilder.append("identifier=").append(s).append("&"));
      var queryString = queryStringBuilder.toString();
      if (queryString.length() > 1 && queryString.endsWith("&")) {
        return queryString.substring(0, queryString.length() - 1);
      }
      return queryString;
    }
  }
}
