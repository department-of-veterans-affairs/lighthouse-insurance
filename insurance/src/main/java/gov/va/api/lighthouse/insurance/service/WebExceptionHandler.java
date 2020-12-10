package gov.va.api.lighthouse.insurance.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.elements.Extension;
import gov.va.api.health.r4.api.elements.Narrative;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@RestControllerAdvice
@RequestMapping(produces = {"application/json"})
public class WebExceptionHandler {
  /** Reconstruct a sanitized URL based on the request. */
  private static String reconstructUrl(HttpServletRequest request) {
    return request.getRequestURI()
        + (request.getQueryString() == null ? "" : "?" + request.getQueryString())
            .replaceAll("[\r\n]", "");
  }

  private OperationOutcome asOperationOutcome(
      String code, Throwable tr, HttpServletRequest request, List<String> diagnostics) {
    OperationOutcome.Issue issue =
        OperationOutcome.Issue.builder()
            .severity(OperationOutcome.Issue.IssueSeverity.fatal)
            .code(code)
            .build();
    String diagnostic = diagnostics.stream().collect(Collectors.joining(", "));
    if (isNotBlank(diagnostic)) {
      issue.diagnostics(diagnostic);
    }
    return OperationOutcome.builder()
        .id(UUID.randomUUID().toString())
        .resourceType("OperationOutcome")
        .extension(extensions(tr, request))
        .text(
            Narrative.builder()
                .status(Narrative.NarrativeStatus.additional)
                .div("<div>Failure: " + request.getRequestURI() + "</div>")
                .build())
        .issue(singletonList(issue))
        .build();
  }

  private List<Extension> extensions(Throwable tr, HttpServletRequest request) {
    List<Extension> extensions = new ArrayList<>(5);
    extensions.add(
        Extension.builder().url("timestamp").valueInstant(Instant.now().toString()).build());
    extensions.add(
        Extension.builder().url("type").valueString(tr.getClass().getSimpleName()).build());
    extensions.add(Extension.builder().url("request").valueString(reconstructUrl(request)).build());
    return extensions;
  }

  @ExceptionHandler({JsonParseException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public OperationOutcome handleBadRequest(Exception e, HttpServletRequest request) {
    return responseFor("bad-request", e, request, emptyList(), true);
  }

  @ExceptionHandler({HttpClientErrorException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public OperationOutcome handleNotFound(Exception e, HttpServletRequest request) {
    return responseFor("not-found", e, request, emptyList(), true);
  }

  /**
   * For exceptions relating to unmarshalling json, we want to make sure no PII is being logged.
   * Therefore, when we encounter these exceptions, we will not print the stacktrace to prevent PII
   * showing up in our logs.
   */
  @ExceptionHandler({Exception.class, UndeclaredThrowableException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public OperationOutcome handleSnafu(Exception e, HttpServletRequest request) {
    return responseFor("exception", e, request, emptyList(), true);
  }

  @SneakyThrows
  private OperationOutcome responseFor(
      String code,
      Throwable tr,
      HttpServletRequest request,
      List<String> diagnostics,
      boolean printStackTrace) {
    OperationOutcome response = asOperationOutcome(code, tr, request, diagnostics);
    if (printStackTrace) {
      log.error("Response {}", JacksonConfig.createMapper().writeValueAsString(response), tr);
    } else {
      log.error("Response {}", JacksonConfig.createMapper().writeValueAsString(response));
    }
    return response;
  }
}
