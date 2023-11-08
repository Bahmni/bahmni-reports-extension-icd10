package org.bahmni.reports.extensions.icd10.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.reports.extensions.icd10.Icd10LookupService;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.bahmni.reports.extensions.util.FhirParserUtil;
import org.bahmni.reports.extensions.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class Icd10LookupServiceImpl implements Icd10LookupService {
    public static final Comparator<IcdRule> customComparator = Comparator.comparingInt((IcdRule rule) -> Integer.parseInt(rule.mapGroup)).thenComparingInt(rule -> Integer.parseInt(rule.mapPriority));
    private static final Logger logger = LogManager.getLogger(Icd10LookupServiceImpl.class);
    private static final String ICD_PROPERTIES_FILENAME = "icd-service-config.properties";
    private static final Properties icd10Properties = loadIcdProperties();
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    private FhirParserUtil fhirParserUtil = new FhirParserUtil();


    static Properties loadIcdProperties() {
        return PropertyUtil.loadProperties(ICD_PROPERTIES_FILENAME);
    }

    @Override
    public List<IcdRule> getRules(String snomedCode) {
        try {
            IcdResponse icdResponse;
            List<IcdRule> icdRules = new ArrayList<>();
            int offset = 0, limit = 100;
            do {
                URI encodedURI = getEndPoint(snomedCode, offset, limit);
                icdResponse = getResponse(encodedURI);
                icdRules.addAll(icdResponse.getItems());
                offset += limit;
            } while (offset < icdResponse.getItems().size());
            return icdRules.stream().sorted(customComparator).collect(Collectors.toList());
        } catch (Exception exception) {
            logger.error(String.format("Error caused during ICD lookup rest call: %s", exception.getMessage()));
            return new ArrayList<>();
        }

    }

    private String getEclUrl(String referencedComponentId) {
        return String.format(icd10Properties.getProperty("icd.eclUrl"), referencedComponentId);
    }

    private String encode(String rawStr) throws UnsupportedEncodingException {
        return URLEncoder.encode(rawStr, StandardCharsets.UTF_8.name());
    }

    private IcdResponse getResponse(URI encodedURI) {
        ResponseEntity<String> responseEntity = restTemplate.exchange(encodedURI, HttpMethod.GET, new org.springframework.http.HttpEntity<>(null, getHeaders()), String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseStr = responseEntity.getBody();
            try {
                Map<String, String> responseMap = mapper.readValue(responseStr, Map.class);
                if ("Parameters".equals(responseMap.get("resourceType"))) {
                    return fhirParserUtil.extractIcdRules(responseStr);
                } else {
                    return mapper.readValue(responseStr, IcdResponse.class);
                }
            } catch (JsonProcessingException e) {
                return new IcdResponse();
            }
        }
        return new IcdResponse();
    }

    private URI getEndPoint(String snomedCode, Integer offset, Integer limit) {
        String icd10LiteIndicator = System.getenv("ICD10_LITE_INDICATOR");
        if (Boolean.parseBoolean(icd10LiteIndicator)) {
            return getLiteEndPoint(snomedCode);
        }
        return getSnowstormEndPoint(snomedCode, offset, limit);
    }

    @NotNull
    private URI getSnowstormEndPoint(String snomedCode, Integer offset, Integer limit) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(getIcdBaseUrl())
                    .queryParam("offset", offset)
                    .queryParam("termActive", true)
                    .queryParam("ecl", encode(getEclUrl(snomedCode)))
                    .queryParam("limit", limit);
            return uriBuilder.build(true).toUri();
        } catch (Exception exception) {
            logger.error("Error while encoding ecl url ", exception);
            throw new RuntimeException(exception);
        }
    }

    @NotNull
    private URI getLiteEndPoint(String snomedCode) {
        String icdLiteUrlTemplate = icd10Properties.getProperty("icd.lite.urlTemplate");
        return URI.create(String.format(icdLiteUrlTemplate, snomedCode));
    }

    private String getIcdBaseUrl() {
        String icd10BaseUrl = System.getenv("ICD10_BASE_URL");
        return (icd10BaseUrl != null) ? icd10BaseUrl : icd10Properties.getProperty("icd.baseUrl");
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

}
