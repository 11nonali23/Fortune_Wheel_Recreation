package useful;

import org.apache.commons.csv.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CSVFileManager {
    private String[] headers;
    private String fileToInsert;
    private Reader inputFile;
    private Iterable<CSVRecord> fileRecord;

    /**
     *Inizializza l'oggetto con un file csv da cui escludere la lettura della prima riga
     */
    public CSVFileManager() {
            this.headers = new String[]{"autore", "genere","frase"};
    }

    public void importFile(String file) throws IOException {
        this.fileToInsert = file;
        this.inputFile = new FileReader(fileToInsert);
        this.fileRecord = CSVFormat.DEFAULT
                .withHeader(headers)
                .withFirstRecordAsHeader()
                .parse(inputFile);
    }
    /*
    *ritorna un array di stringhe con autore frase e genere
     */
    public List<String> readLines(){
        System.out.println("csv file manager:\n" +
                "le frasi più lunghe di 60 caratteri verranno eliminate");
        List<String> lines = new ArrayList<String>();
        for(CSVRecord rec : fileRecord){
            if(rec.get("frase").length()<60){
                lines.add(rec.get("frase"));
                lines.add(rec.get("tema"));
                lines.add(rec.get("creatore"));
            }
        }
         for(String line: lines)
            System.out.println(line);
        return lines;
    }
}
