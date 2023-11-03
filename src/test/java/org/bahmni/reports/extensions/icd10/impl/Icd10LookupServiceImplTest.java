package org.bahmni.reports.extensions.icd10.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;

import org.bahmni.reports.extensions.util.FileReaderUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class Icd10LookupServiceImplTest {
    private static final String SNOMED_CODE = "dummy";
    private static ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    Icd10LookupServiceImpl icd10LookupService;

    @Mock
    ResponseEntity<String> mockIcdResponseStr;

    @Mock
    RestTemplate restTemplate;

    public static IcdResponse getMockSnowstormIcdResponse(String relativePath) {
        try {
            return mapper.readValue(FileReaderUtil.getFileContent(relativePath), IcdResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMockFhirIcdResponse(String relativePath) {
        return FileReaderUtil.getFileContent(relativePath);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnIcdRulesOrderedByMapGroupAndThenMapPriority_WhenValidSnomedCodeIsPassed_ToSnowstorm() throws JsonProcessingException {
        IcdResponse mockResponse = getMockSnowstormIcdResponse("terminologyServices/icdRules_MultipleMapGroups.json");
        String mockResponseStr = new ObjectMapper().writeValueAsString(mockResponse);
        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(mockIcdResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponseStr.getBody()).thenReturn(mockResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponseStr.getBody()).thenReturn(mockResponseStr);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        assertEquals(4, sortedRules.size());
        assertEquals("1", sortedRules.get(0).getMapGroup());
        assertEquals("1", sortedRules.get(0).getMapPriority());
        assertEquals("1", sortedRules.get(1).getMapGroup());
        assertEquals("2", sortedRules.get(1).getMapPriority());
        assertEquals("2", sortedRules.get(2).getMapGroup());
        assertEquals("1", sortedRules.get(2).getMapPriority());
        assertEquals("2", sortedRules.get(3).getMapGroup());
        assertEquals("2", sortedRules.get(3).getMapPriority());
    }

    @Test
    public void shouldReturnIcdRulesOrderedByMapGroupAndThenMapPriority_WhenValidSnomedCodeIsPassed_ToSnowstormLite() {
        String mockResponseStr = getMockFhirIcdResponse("terminologyServices/icdRules_MultipleMapGroups_Lite.json");
        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(mockIcdResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponseStr.getBody()).thenReturn(mockResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponseStr.getBody()).thenReturn(mockResponseStr);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        assertEquals(2, sortedRules.size());
        assertEquals("1", sortedRules.get(0).getMapGroup());
        assertEquals("1", sortedRules.get(0).getMapPriority());
        assertEquals("TRUE", sortedRules.get(0).getMapRule());
        assertEquals("B20.8", sortedRules.get(0).getMapTarget());
        assertEquals("2", sortedRules.get(1).getMapGroup());
        assertEquals("1", sortedRules.get(1).getMapPriority());
        assertEquals("TRUE", sortedRules.get(1).getMapRule());
        assertEquals("J17.8", sortedRules.get(1).getMapTarget());
    }

    @Test
    public void shouldReturnEmptyList_WhenInvalidSnomedCodeIsPassed() {
        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(mockIcdResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        assertEquals(0, sortedRules.size());
    }

    @Test
    public void shouldInvokePaginatedCalls_WhenIcdRulesHasLargeResultSet() throws JsonProcessingException {
        IcdResponse mockResponse = getMockSnowstormIcdResponse("terminologyServices/icdRules_WithLargeResultSet.json");
        String mockResponseStr = new ObjectMapper().writeValueAsString(mockResponse);
        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(mockIcdResponseStr);
        when(mockIcdResponseStr.getStatusCode()).thenReturn(HttpStatus.ACCEPTED);
        when(mockIcdResponseStr.getBody()).thenReturn(mockResponseStr);
        List<IcdRule> sortedRules = icd10LookupService.getRules(SNOMED_CODE);
        assertNotNull(sortedRules);
        verify(restTemplate, times(2)).exchange(any(), any(), any(), eq(String.class));
    }

}
