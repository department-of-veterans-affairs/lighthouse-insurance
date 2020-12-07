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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

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
    return Coverage.builder()
        .resourceType("Coverage")
        .id("I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289")
        .status(Coverage.Status.active)
        .subscriberId("1092387456V321456")
        .subscriber(
            Reference.builder()
                .reference(basepath + "r4/Patient/1092387456V321456")
                .display("JOHN Q VETERAN")
                .id("1092387456V321456")
                .build())
        .beneficiary(
            Reference.builder()
                .reference(basepath + "r4/Patient/1092387456V321456")
                .display("JOHN Q VETERAN")
                .build())
        .relationship(
            CodeableConcept.builder()
                .coding(
                    List.of(
                        Coding.builder()
                            .system("http://terminology.hl7.org/CodeSystem/subscriber-relationship")
                            .code("self")
                            .display("Self")
                            .build()))
                .build())
        .payor(
            List.of(
                Reference.builder()
                    .reference(basepath + "r4/Organization/I2-1PQBLFRZ6207KNR9KLJTLHA5RY000903")
                    .display("EXH INSURANCE")
                    .build()))
        .coverageClass(
            List.of(
                Coverage.CoverageClass.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .system(
                                            "http://terminology.hl7.org/CodeSystem/coverage-class")
                                        .code("group")
                                        .display("Group")
                                        .build()))
                            .build())
                    .value("Group")
                    .build()))
        .order(1)
        .costToBeneficiary(
            List.of(
                Coverage.CostToBeneficiary.builder()
                    .type(
                        CodeableConcept.builder()
                            .coding(
                                List.of(
                                    Coding.builder()
                                        .system(
                                            "http://terminology.hl7.org/CodeSystem/coverage-copay-type")
                                        .code("gpvisit")
                                        .display("GP Office Visit")
                                        .build()))
                            .build())
                    .valueMoney(
                        Money.builder().currency("USD").value(BigDecimal.valueOf(100)).build())
                    .build()))
        .build();
  }

  /** Read coverage by ID. */
  @GetMapping(value = "/{id}")
  Coverage readCoverageId(@PathVariable("id") String id) {
    if (!"I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289".equals(id)) {
      throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
    }
    return buildCoverage();
  }

  /** Get coverage by search parameters. */
  @GetMapping
  Coverage.Bundle readCoverageSearchParameters(
      @RequestParam(value = "_id", required = false) String id,
      @RequestParam(value = "patient", required = false) String patient,
      @RequestParam(value = "identifier", required = false) String identifier) {
    if (id != null && !"I2-8TQPWFRZ4792KNR6KLYYYHA5RY000289".equals(id)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
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
