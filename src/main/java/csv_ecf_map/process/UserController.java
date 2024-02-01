package csv_ecf_map.process;

import csv_ecf_map.utils.fileUtility.CustomGenerationFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import static csv_ecf_map.Constants.*;

/**
 * Created by c-sergluca on 10/18/2018.
 */
public class UserController {
    public static void processPerClaim(List<File> files, String claimType){
        Scanner scanner = new Scanner(System.in);
        if (generateDefaultMacrosTOecfFiles) {
            String absolutePathfile = MACROS_ECFFile_MODEL.replace(".properties","_"+claimType+".properties");
            String absolutePathfile1 = ECF_VALUESFile_MODEL.replace(".properties","_"+claimType+".properties");
            System.out.println("\n************************************ "+claimType+"Claim ******************************************************");
            System.out.println("\nNew file properties <<Macros>>-to-<<ECF paths>> and <<ECF paths>>-to-<<Values>> will be created.\nTheir location:\n\n\t\t\"" + absolutePathfile + "\"");
            System.out.println("\t\t\"" + absolutePathfile1 + "\"\n\nIf this file already exists, you will lose the previous configs.\nAre you sure? [Y/N]");
            String s = scanner.next();
            if (s.equalsIgnoreCase("y")) {
                String ecf_filePath = files.stream().filter(i->i.getName().contains("xml")).findFirst().get().getAbsolutePath();
                LinkedHashMap<String, String> parsedXMLPaires = SAXParsing.parse(ecf_filePath);
                generateMacrosFile(parsedXMLPaires, absolutePathfile);
                generateECFvaluesFile(parsedXMLPaires, absolutePathfile1);
                generateECFvaluesFile(parsedXMLPaires, ECF_VALUEScustom_MODEL.replace(".properties","_"+claimType+".properties"));
            }
        }
        if(generateCSV_with_Macros){
            String absolutePathfile = CSV_MACROSFile_MODEL.replace(CSVKey,claimType+"Claim");
            System.out.println("\n********************************************************************************************************");
            System.out.println("\nNew file CSV with Macros will be created. Its location:\n\n\t\t\"" + absolutePathfile + "\"\n\nIf this file already exists, " +
                    "you will lose the previous configs.\nAre you sure? [Y/N]");

            String s = scanner.next();
            if (s.equalsIgnoreCase("y")) generateCSVMacrosFile(absolutePathfile,claimType);
        }
    }

    public static void generateMacrosFile(LinkedHashMap<String, String> parsedXMLPaires, String absolutePath) {
        int h = 1;
        StringBuilder sb = new StringBuilder();
        for (String s : parsedXMLPaires.keySet()) {
            sb.append("{").append(h++).append("}=").append(s).append(delimiter);
        }
        CustomGenerationFile.createFile(sb, absolutePath);
    }

    public static void generateECFvaluesFile(LinkedHashMap<String, String> parsedXMLPaires, String absolutePath) {
        StringBuilder sb = new StringBuilder();
        parsedXMLPaires.keySet().stream().forEach(s->sb.append(s).append("=").append(parsedXMLPaires.get(s)).append(delimiter));
        CustomGenerationFile.createFile(sb, absolutePath);
    }

    public static void generateCSVMacrosFile(String absolutePath, String claimType) {
        String correctTemplateCSV =String.format(TEMPLATES_FOLDER+"\\CSV_%s.dat", claimType);
        StringBuilder sb = MacrosCSVUtility.replaceVAlues_with_Macros(new File(correctTemplateCSV),claimType);
        CustomGenerationFile.createFile(sb, absolutePath);
    }

}
