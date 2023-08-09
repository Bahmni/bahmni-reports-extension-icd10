package org.bahmni.reports.util;


import org.bahmni.reports.extensions.util.FileReaderUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
public class FileReaderUtilTest {
    @Test
    public void shouldReadFileContent() {
        String content = FileReaderUtil.getFileContent("terminologyServices/sampleSqlFile.sql");
        assertTrue(content.contains("This is a sample sql file"));

        content = FileReaderUtil.getFileContent("terminologyServices/sampleSqlFile.sql", false);
        assertTrue(content.contains("This is a sample sql file"));
    }

    @Test
    public void shouldReadFileContentFromAbsoluteFilePath() throws Exception {
        String content = FileReaderUtil.getFileContent("src/test/sql/test.sql", true);
        assertTrue(content.contains("select * from someTable"));
        assertTrue(content.contains("where a > 10"));
    }
}
