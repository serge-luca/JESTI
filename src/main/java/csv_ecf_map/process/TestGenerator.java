package csv_ecf_map.process;

import csv_ecf_map.utils.fileUtility.LoadUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static csv_ecf_map.Constants.*;

/**
 * Created by c-sergluca on 10/22/2018.
 */
public class TestGenerator {

    public static void createTests() {
        if (!generateTests) {
            System.out.println("Test generation is disabled");
            return;
        }
        File f;
        if ((!(f = new File(TEMPLATES_FOLDER)).exists() || f.listFiles() == null) ||
                (!(f = new File(DEFAULT_XPATH_VALUES)).exists() || f.listFiles() == null)) {
            System.out.println("Error: The project folder content is not enough for tests creation\nPlease check templates and " +
                    "generate configs first.\nHint: for configs generation set the following constants to true:\n\t\tgenerateDefaultMacrosTOecfFiles" +
                    "\n\t\tgenerateCSV_with_Macros\n\nAfter that - change macros into generated files in the way you want.");
            return;
        }
        ;

        System.out.println(LocalTime.now() + " ************** Tests creation start");
        List<FuckingTestCase> fuckingTestCases = FuckingTestCase.generate();
        fuckingTestCases.forEach(TestGenerator::generateSingleFuckingTest);
        System.out.println(LocalTime.now() + " ************** Tests creation finish\nBasical testPath=" + OUTPUT_TESTS_DIR);
    }

    /**
     * Replace all macros from rootPath/Generated/MacrosCSV with values from rootPath/Generated/Configs
     * except marosReplace map => this macros are added/replaced from custXPathsReplaceMap
     */
    public static String populateMacrosCSV(String claimType, Map<String, String> custXPathsReplaceMap) {
        if (custXPathsReplaceMap == null) custXPathsReplaceMap = new HashMap<>();
        Map<String, String> inversMap = MacrosCSVUtility.getECF_to_MacrosMap(claimType);
        String[] st = new String[1];
        Map<String, String> finalCustXPathsReplaceMap = custXPathsReplaceMap;
        custXPathsReplaceMap = custXPathsReplaceMap.keySet().stream().filter(inversMap::containsKey)
                .map(i -> {
                            st[0] = i;
                            return inversMap.get(i);
                        }
                ).collect(Collectors.toMap(i -> i, k -> finalCustXPathsReplaceMap.get(st[0])));

        String absolutePathfile = CSV_MACROSFile_MODEL.replace(CSVKey, claimType + "Claim");
        StringBuilder csv_content = CacheMaps.getContentSBMap(absolutePathfile);
        Map<String, String> macros_to_Values_prop = MacrosCSVUtility.getMacrosToValuesPop(claimType, true);
        List<String> list = new ArrayList<>(Arrays.asList(csv_content.toString().split(fieldDelimiter)));
        Set<String> mostImportant = custXPathsReplaceMap.keySet();
        boolean found;
        for (int i = 0; i < list.size(); i++) {
            String temp = list.get(i);
            temp=temp.replace("?", "");
            found = false;
            for (String s : mostImportant) {
                if (temp.contains(s)) {
                    temp = removeRedundantMacros(temp.replace(s, custXPathsReplaceMap.get(s)), true);
                    found = true;
                    break;
                }
            }
            if (!found && temp.contains("{")) {
                temp = removeRedundantMacros(temp, false);
                int j = -1;
                while (temp.contains("{")) {
                    if (j == (j = temp.indexOf("{"))) break;
                    String s = temp.substring(temp.indexOf("{"), temp.indexOf("}") + 1);
                    if (macros_to_Values_prop.get(s) != null) temp = temp.replace(s, macros_to_Values_prop.get(s));
                }
                found = true;
                list.set(i, temp);
            }
            if (found) {
                list.set(i, temp);
            }
        }
        return list.stream().collect(Collectors.joining(fieldDelimiter));
    }

    public static String removeRedundantMacros(String s, boolean all) {
        if (s.contains("{")) {
            StringBuilder sb = new StringBuilder();
            boolean firstMacro = false;
            boolean macroEnd = false;
            boolean ignore = false;
            for (char c : s.toCharArray()) {
                if (c == '{') {
                    if (macroEnd)
                        if (!ignore) ignore = true;
                    macroEnd = false;
                    firstMacro = true;
                } else if (c == '}') {
                    macroEnd = true;
                    firstMacro = false;
                } else if (macroEnd) {
                    macroEnd = false;
                    if (!ignore) ignore = true;
                }
                if (all) {
                    if (!firstMacro && !macroEnd) sb.append(c);
                } else {
                    if (!ignore) sb.append(c);
                    if (ignore && !firstMacro && !macroEnd) sb.append(c);
                }
            }
            s = sb.toString();
        }
        return s;
    }

    private static void generateSingleFuckingTest(FuckingTestCase fuckingTestCase) {
        new File(OUTPUT_TESTS_DIR + fuckingTestCase.getErrorID()).mkdirs();

        for (char claimType : fuckingTestCase.getClaimTypes()) {
            String filePath = OUTPUT_TESTS_DIR + fuckingTestCase.getErrorID() + (longPathTests ? File.separator + ClaimType.getClaimTypeTest(claimType) : "");


            new File(filePath).mkdirs();
            for (int i = 0; i != fuckingTestCase.getScenarioList().length; ++i) {
                String scenarioPath = filePath + (longPathTests ? File.separator + scenarioPrefix + (i + 1) : "");
                new File(scenarioPath).mkdirs();
                String clType = ClaimType.getClaimType(claimType);

                try {
                    Map<String, String> customXpathsMap = fuckingTestCase.getScenarioListMap().get(i);
                    StringBuilder customXPaths = new StringBuilder();
                    if (customXpathsMap != null)
                        customXpathsMap.forEach((path, value) -> {
                            if(value.contains("{}")) value=value.replace("{}","");
                            if(value.equals(""))customXPaths.append("boolean(").append(path).append(")").append("=").append("false").append(delimiter);
                            else if(value.trim().length()==0) customXPaths.append("boolean(").append(path).append(")").append("=").append("true").append(delimiter);
                            else customXPaths.append(path).append("=").append(value).append(delimiter);
                        });

                    String claim = populateMacrosCSV(clType, customXpathsMap);

                    String testCaseName = testPrefix + clType + (longPathTests ? testSecondPrefix : "") + fuckingTestCase.getErrorID() + "Sc" + (i + 1) + "_1_CSV.dat";
                    //write csv
                    BufferedWriter writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName));
                    writer.write(claim);
                    writer.close();
                    writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMClaim_props_cl0.properties"));
                    writer.write(CLAIM_PROPS);
                    writer.close();
                    writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMClaim_xpath_cl0.properties"));
                    writer.write(customXPaths.toString());
                    writer.close();
                    writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_xpath_cl0.properties"));
                    writer.write(customXPaths.toString().replace(")=true",")=false"));
                    writer.close();
                    if (fuckingTestCase.getScenarioList()[i] == '+') {
                        //positive
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMClaim_events_cl0.properties"));
                        writer.write(POSTIVE_CLAIM_EVENTS);
                        writer.close();
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_events_cl0_en0.properties"));
                        writer.write(POSTIVE_ENCOUNTER_EVENTS);
                        writer.close();
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_props_cl0_en0.properties"));
                        writer.write(POSTIVE_ENCOUNTER_PROPS);
                        writer.close();
                    } else {
                        //negative
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMClaim_events_cl0.properties"));
                        writer.write(NEGATIVE_CLAIM_EVENTS);
                        writer.close();
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_events_cl0_en0.properties"));
                        writer.write(NEGATIVE_ENCOUNTER_EVENTS);
                        writer.close();
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_props_cl0_en0.properties"));
                        writer.write(NEGATIVE_ENCOUNTER_PROPS);
                        writer.close();
                        writer = new BufferedWriter(new FileWriter(scenarioPath + File.separator + testCaseName + "_assert_RRMEncounter_errors_cl0_en0.properties"));
                        writer.write(ERROR_CONTENT);
                        writer.close();
                    }
                } catch (IOException e) {
                    System.out.println("Cannot create fucking test!!!!");
                    System.out.println(e);
                }
            }
        }
    }


    static class CacheMaps {
        static Map<String, StringBuilder> contentSBMap = new HashMap<>();

        public static StringBuilder getContentSBMap(String absolutePathfile) {
            if (contentSBMap.get(absolutePathfile) == null)
                contentSBMap.put(absolutePathfile, LoadUtility.getContentSBfromFile(new File(absolutePathfile)));
            return contentSBMap.get(absolutePathfile);
        }
    }

}
