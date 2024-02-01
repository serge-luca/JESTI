package csv_ecf_map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Created by c-sergluca on 10/18/2018.
 */
public class Constants {
//properties.load(MainClass.class.getResourceAsStream("1_Project/constants.properties"))
    //B:\JESTI2
    private static Properties properties = new Properties();
    private static Path root;
    private static String temp;
    public static String rootPath;
    //input CSV:
    public static String INPUT_FILE;

    static {
        try {
            root = Paths.get(".").toRealPath();
            if(root.getFileName().toString().equals("lib")) root = Paths.get("../../").toRealPath();
            properties.load(new BufferedReader(new FileReader(Paths.get(root.toString(),"/1_Configs/constants.properties").toString())));

            temp=properties.getProperty("rootPath");
            rootPath = (temp=properties.getProperty("rootPath")) == null ? root.toString() :temp;

            if((temp=properties.getProperty("INPUT_FILE")) == null) temp = "1_Configs/input.csv";
            INPUT_FILE = Paths.get(rootPath).resolve(Paths.get(temp)).toRealPath().toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Aici setam true ce lipseste in project:
    public static boolean generateDefaultMacrosTOecfFiles = (temp=properties.getProperty("generateDefaultMacrosTOecfFiles")) == null ? false : Boolean.valueOf(temp);
    public static boolean generateCSV_with_Macros = (temp=properties.getProperty("generateCSV_with_Macros")) == null ? false : Boolean.valueOf(temp);
    public static boolean generateTests = (temp=properties.getProperty("generateTests")) == null ? false : Boolean.valueOf(temp);
    public static boolean longPathTests = (temp=properties.getProperty("longPathTests")) == null ? false : Boolean.valueOf(temp);            //=> vrem toate testele intr-un folder sau pe Scenario1/2 etc ?


    public static String testPrefix = (temp=properties.getProperty("testPrefix")) == null ? "" :temp;
    public static String testSecondPrefix = (temp=properties.getProperty("testSecondPrefix")) == null ? "Error" :temp;

    public static String generatedFiles = rootPath + "\\1_Configs";
    public static String TEMPLATES_FOLDER = generatedFiles + "\\Templates";

    public static final String testFolderIndex = LocalTime.now().format(DateTimeFormatter.ofPattern("HH.mm.ss"));
    public static final String OUTPUT_TESTS_DIR = rootPath +"\\4_GeneratedTests_"+ testFolderIndex +"\\";

    public static String DEFAULT_XPATH_VALUES = generatedFiles +"\\XPATHS";
    public static String MACROS_ECFFile_MODEL = generatedFiles +"\\XPATHS\\Macros_XPaths.properties";
    public static String ECF_VALUESFile_MODEL = generatedFiles +"\\XPATHS\\XPaths.properties";
    public static String ECF_VALUEScustom_MODEL = generatedFiles +"\\XPATHS\\CustomValues\\XPaths.properties";

    public static String CSVKey = "CSVModel";
    public static String CSV_MACROSFile_MODEL = generatedFiles + "\\MacrosCSV\\"+CSVKey+"_1_CSV.dat";

    public static String scenarioPrefix = (temp=properties.getProperty("scenarioPrefix"))==null ? "Scenario" : temp;
    public static String delimiter = ((temp=properties.getProperty("lineDelimiter"))==null) ? "\r\n" : temp.toUpperCase().replace("CR","\r").replace("LF","\n");
    public static String fieldDelimiter = ",";

    public final static String POSTIVE_CLAIM_EVENTS = "EventTopic=EncounterGeneration&TriggerEncounterSuccess";
    public final static String CLAIM_PROPS = "ActivityState=CP" + delimiter+
            "Disposition=In Progress";
    public final static String POSTIVE_ENCOUNTER_EVENTS = "EventTopic=EncounterGeneration&EncounterGeneration";
    public final static String POSTIVE_ENCOUNTER_PROPS = "ActivityState=CR" + delimiter +
            "Disposition=In Progress";

    public final static String NEGATIVE_CLAIM_EVENTS = "EventTopic=RejectEncounter";
    public final static String NEGATIVE_ENCOUNTER_EVENTS = "EventTopic=RejectEncounter";
    public final static String NEGATIVE_ENCOUNTER_PROPS =  "ActivityState=CPX" + delimiter +
            "Disposition=Rejected";

    public final static String ERROR_CONTENT = "ErrorIdentifier=" + delimiter +
            "BizErrorMessage=" + delimiter +
            "ErrorLocation=";

    public Constants() throws IOException {
    }


    public static enum ClaimType{
        Dent("837D"),Inst("837I"),Prof("837P"),Pharm("NCPDP");
        String claimTypeTest;

        ClaimType(String s) {
            claimTypeTest=s;
        }

        public static String getClaimTypeTest(char claimType){
            String clType = getClaimType(claimType);
            return ClaimType.valueOf(clType).claimTypeTest;
        }
        public static String getClaimType(char scurt) {
            switch(scurt) {
                case 'I' :
                    return Inst.name();
                case 'P' :
                    return Prof.name();
                case 'D' :
                    return Dent.name();
                case 'R' :
                    return Pharm.name();
            }
            return "";
        }
    }
}
