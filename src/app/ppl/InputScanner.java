package app.ppl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class InputScanner {

    File inputFile;
    ArrayList<String> inputSymbols;

    public InputScanner(String fileName) {
        this.inputFile = new File(fileName);
        this.inputSymbols = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(this.inputFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                Scanner scanner = new Scanner(line);

                this.inputSymbols.add(scanner.next());

                scanner.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(this.inputSymbols.toString());
    }

    public ArrayList<String> getSymbols() {
        return this.inputSymbols;
    }
}