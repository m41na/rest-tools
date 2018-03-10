package works.hop.rest.tools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvToTable {

    private static final Logger LOG = LoggerFactory.getLogger(CsvToTable.class);
    private final String csvFile;

    public CsvToTable(String csvFile) {
        super();
        this.csvFile = csvFile;
    }

    public String genTable() {
        StringBuilder builder = new StringBuilder("<tbody>").append("\r\n");
        if (csvFile != null) {
            File file = new File(csvFile);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ((line = line.trim()).length() > 0) {
                        String[] split = line.split("\t");
                        builder.append("<tr>").append("\r\n");
                        for (String item : split) {
                            if (item.length() > 0) {
                                builder.append("\t<td>").append(item).append("</td>").append("\r\n");
                                LOG.info("adding " + item);
                            }
                        }
                        builder.append("</tr>").append("\r\n");
                    }
                }
                return builder.append("</tbody>").append("\r\n").toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("currently just using file paths");
        }
    }

    public static void main(String[] args) {
        System.out.println(new CsvToTable("C:\\Users\\0018038\\Documents\\endpoints-access-levels.txt").genTable());
    }
}
