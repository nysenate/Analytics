package gov.nysenate.analytics.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.ini4j.Profile.Section;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVReport
{
    protected static CSVWriter getCSVWriter(Section params) throws IOException
    {
        File outputFile = new File(params.get("output_file"));
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new IOException("Unable to make parent directories for: " + outputFile.getAbsolutePath());
        }
        else {
            return new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
        }
    }
}
