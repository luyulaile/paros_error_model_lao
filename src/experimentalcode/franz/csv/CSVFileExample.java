package experimentalcode.franz.csv;

import java.util.*;
import java.io.*;

public class CSVFileExample {

	public static void main(String[] args) throws FileNotFoundException,IOException {

		CSVFileReader in = new CSVFileReader("csv_in.txt", ';', '"');
		CSVFileWriter out = new CSVFileWriter("csv_out.txt", ',', '\'');

    List<String> fields = in.readFields();
    while(fields!=null) {
      out.writeFields(fields);
      fields = in.readFields();
    }

    in.close();
    out.close();
 }

}

