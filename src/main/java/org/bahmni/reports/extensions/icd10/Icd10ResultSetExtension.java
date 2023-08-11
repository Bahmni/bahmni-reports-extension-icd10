package org.bahmni.reports.extensions.icd10;


import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import org.bahmni.reports.extensions.ResultSetExtension;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Map;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;



public class Icd10ResultSetExtension implements ResultSetExtension {
    public static final String ICD_10_COLUMN_NAME = "ICD10 Code(s)";
    public static final String PATIENT_DATE_OF_BIRTH_COLUMN_NAME = "Date of Birth";
    public static final String GENDER_COLUMN_NAME = "Gender";
    public static final String TERMINOLOGY_COLUMN_NAME = "Terminology Code";
    public static StyleBuilder columnStyle;

    public Icd10Evaluator icd10Evaluator = new Icd10Evaluator();

    public void enrich(Collection<Map<String, ?>> collection, JasperReportBuilder jasperReport) {
        collection.forEach(this::enrichRowWithIcdCode);
        jasperReport.addColumn(col.column(ICD_10_COLUMN_NAME, ICD_10_COLUMN_NAME, type.stringType()).setStyle(columnStyle).setHorizontalAlignment(HorizontalAlignment.CENTER));
    }

    private void enrichRowWithIcdCode(Map<String, ?> rowMap) {
        String terminologyCode = (String) rowMap.get(TERMINOLOGY_COLUMN_NAME);
        int age = getAgeFromDob((String) rowMap.get(PATIENT_DATE_OF_BIRTH_COLUMN_NAME));
        String gender = (String) rowMap.get(GENDER_COLUMN_NAME);
        String icd10Code = icd10Evaluator.getMatchingIcdCodes(terminologyCode, age, gender);
        enrichRow(rowMap, ICD_10_COLUMN_NAME, icd10Code);
    }

    private int getAgeFromDob(String dateOfBirth) {
        return Period.between(LocalDate.parse(dateOfBirth), LocalDate.now()).getYears();
    }

    private static class reportStyle {
        {
            columnStyle = stl.style().setPadding(2)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        }
    }
}
