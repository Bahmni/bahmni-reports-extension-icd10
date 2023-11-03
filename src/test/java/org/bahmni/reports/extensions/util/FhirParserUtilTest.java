package org.bahmni.reports.extensions.util;

import org.bahmni.reports.extensions.icd10.bean.IcdResponse;
import org.bahmni.reports.extensions.icd10.bean.IcdRule;
import org.bahmni.reports.extensions.icd10.impl.Icd10LookupServiceImplTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FhirParserUtilTest {
    FhirParserUtil fhirParserUtil = new FhirParserUtil();

    @Test
    public void shouldReturnIcdRulesOrderedByMapPriority_ForSingleMapGroup() {
        String mockResponseStr = Icd10LookupServiceImplTest.getMockFhirIcdResponse("terminologyServices/icdRules_SingleMapGroup_Lite.json");
        IcdResponse icdResponse = fhirParserUtil.extractIcdRules(mockResponseStr);
        List<IcdRule> sortedRules = icdResponse.getItems();
        assertNotNull(sortedRules);
        assertEquals(3, sortedRules.size());
        assertEquals("1", sortedRules.get(0).getMapGroup());
        assertEquals("1", sortedRules.get(0).getMapPriority());
        assertEquals("IFA 248152002 | Female (finding) |", sortedRules.get(0).getMapRule());
        assertEquals("N97.9", sortedRules.get(0).getMapTarget());
        assertEquals("1", sortedRules.get(1).getMapGroup());
        assertEquals("2", sortedRules.get(1).getMapPriority());
        assertEquals("IFA 248153007 | Male (finding) |", sortedRules.get(1).getMapRule());
        assertEquals("N46", sortedRules.get(1).getMapTarget());
        assertEquals("1", sortedRules.get(2).getMapGroup());
        assertEquals("3", sortedRules.get(2).getMapPriority());
        assertEquals("OTHERWISE TRUE", sortedRules.get(2).getMapRule());
        assertEquals(null, sortedRules.get(2).getMapTarget());
    }

    @Test
    public void shouldReturnIcdRulesOrderedByMapGroupAndThenMapPriority_ForMultipleMapGroups() {
        String mockResponseStr = Icd10LookupServiceImplTest.getMockFhirIcdResponse("terminologyServices/icdRules_MultipleMapGroups_Lite.json");
        IcdResponse icdResponse = fhirParserUtil.extractIcdRules(mockResponseStr);
        List<IcdRule> sortedRules = icdResponse.getItems();
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

}
