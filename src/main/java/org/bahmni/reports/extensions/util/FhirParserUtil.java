package org.bahmni.reports.extensions.util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FhirParserUtil {
    private static final String CONCEPT = "concept";
    private static final String MESSAGE = "message";
    private static final String MATCH = "match";
    private static final String MESSAGE_REGEX = "Please observe the following map advice\\. Group:([0-9]+), Priority:([0-9]+), Rule:([^,]+), Advice:'([^']*)', Map Category:'([^']*)'.";
    private IParser fhirJsonParser = FhirContext.forR4().newJsonParser();

    public IcdResponse extractIcdRules(String fhirRulesJson) {
        IcdResponse icdResponse = new IcdResponse();
        try {
            Parameters parameters = fhirJsonParser.parseResource(Parameters.class, fhirRulesJson);
            List<Parameters.ParametersParameterComponent> messageParameters = parameters.getParameter().stream().filter(parameter -> parameter.getName().equals(MESSAGE)).collect(Collectors.toList());
            List<Parameters.ParametersParameterComponent> matchParameters = parameters.getParameter().stream().filter(parameter -> parameter.getName().equals(MATCH)).collect(Collectors.toList());
            List<IcdRule> icdRules = IntStream.range(0, messageParameters.size()).mapToObj(i -> composeIcdRule(messageParameters.get(i), matchParameters.get(i))).collect(Collectors.toList());
            icdResponse.setItems(icdRules);
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing ICD response", e);
        }
        return icdResponse;
    }

    private IcdRule composeIcdRule(Parameters.ParametersParameterComponent messageParameter, Parameters.ParametersParameterComponent matchParameter) {
        IcdRule icdRule = new IcdRule();
        composeIcdRuleFromMessageParameter(icdRule, messageParameter);
        composeIcdRuleFromMatchParameter(icdRule, matchParameter);
        return icdRule;
    }

    private void composeIcdRuleFromMatchParameter(IcdRule icdRule, Parameters.ParametersParameterComponent matchParameter) {
        icdRule.setMapTarget(extractConceptCode(matchParameter));
    }

    private void composeIcdRuleFromMessageParameter(IcdRule icdRule, Parameters.ParametersParameterComponent messageParameter) {
        String message = messageParameter.getValue().toString();
        Pattern pattern = Pattern.compile(MESSAGE_REGEX);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String group = matcher.group(1);
            String priority = matcher.group(2);
            String rule = matcher.group(3);
            icdRule.setMapGroup(group);
            icdRule.setMapPriority(priority);
            icdRule.setMapRule(rule);
        }
    }

    private String extractConceptCode(Parameters.ParametersParameterComponent matchParameter) {
        List<Parameters.ParametersParameterComponent> parts = matchParameter.getPart();
        Parameters.ParametersParameterComponent partParameter = parts.stream().filter(part -> part.getName().equals(CONCEPT)).findFirst().get();
        return ((Coding) partParameter.getValue()).getCode();
    }
}

