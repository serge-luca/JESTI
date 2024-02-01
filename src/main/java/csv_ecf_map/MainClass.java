package csv_ecf_map;

import csv_ecf_map.process.MacrosCSVUtility;
import csv_ecf_map.process.SAXParsing;
import csv_ecf_map.process.TestGenerator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Scanner;

import static csv_ecf_map.Constants.delimiter;

/**
 * Created by c-sergluca on 10/18/2018.
 */
public class MainClass {

   public static void main(String... args){
        if(args.length > 0){
            if(args[0].equalsIgnoreCase("-xpath")) {
                if(args.length>1)
                generateXPaths(args[1]);
                else{
                    Scanner scanner = new Scanner(System.in).useDelimiter(System.getProperty("line.separator"));//for cmd
                    System.out.println("Please indicate absolutePath for ECF file:");
                    generateXPaths(scanner.next());
                }
            }
        }
        else {
            MacrosCSVUtility.generate_macros_CSVs();
            TestGenerator.createTests();
        }
    }

    public static void generateXPaths(String absolutPathToECF){
        String parent = Paths.get(absolutPathToECF).getParent().toString();
        String fileName = Paths.get(absolutPathToECF).getFileName().toString().replace(".","_xpaths.").replace("xml","properties");
        LinkedHashMap<String, String> xmlElements = SAXParsing.parse(absolutPathToECF);
        try {
            String s, s1;
            PrintWriter printWriter = new PrintWriter((s=Paths.get(parent,fileName).toString()));
            printWriter.write((s1 = xmlElements.toString()).substring(1, s1.length()-1).replace(", ",delimiter));
            printWriter.close();
            System.out.printf("XPaths generated into %s file \n",s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
