package gov.nysenate.analytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ini4j.Profile.Section;

public class CSVReport
{
    protected static BufferedWriter getOutputWriter(Section params) throws IOException
    {
        File outputFile = new File(params.get("output_file"));
        if (!outputFile.getParentFile().exists() && !outputFile.mkdirs()) {
            throw new IOException("Unable to make parent directories for: " + outputFile.getAbsolutePath());
        }
        else {
            return new BufferedWriter(new FileWriter(outputFile));
        }
    }
}
