package csv_ecf_map.process;

import csv_ecf_map.Constants;

import java.io.*;
import java.util.*;

public class FuckingTestCase {
    private LinkedHashMap<Integer, Map<String, String>> scenarioListMap = new LinkedHashMap<>();
    private char[] scenarioList = new char[0];
    private String errorID;
    private char[] claimTypes;

    public char[] getClaimTypes() {
        return claimTypes;
    }

    public char[] getScenarioList() {
        return scenarioList;
    }

    public String getErrorID() {
        return errorID;
    }

    public LinkedHashMap<Integer, Map<String, String>> getScenarioListMap() {
        return scenarioListMap;
    }

    public FuckingTestCase() {
    }

    public void setErrorID(String errorID) {
        this.errorID = errorID;
    }

    public void setClaimTypes(String claimTypes) {
        this.claimTypes = claimTypes.toUpperCase().toCharArray();
    }

    public void addScenarioList(String scenarioList) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.scenarioList);
        sb.append(scenarioList.toCharArray());
        this.scenarioList = sb.toString().toCharArray();
    }

    public void addScenarioListMap(int[] positions, Map<String, String> macro) {
        for (int i : positions) {
            scenarioListMap.merge(i, macro, (a, b) -> {
                a.putAll(b);
                return a;
            });
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("***************\nErrorID=").append(errorID).append("\nclaimTypes=").append(claimTypes);
        sb.append("\nTotal scenarios:").append(scenarioList);
        sb.append("\nMacros:\n").append(scenarioListMap.toString().replace(", ", "\n"));
        return sb.toString();
    }

    //*232,PD
    //---
    //echcf\:Claim[1]/echcf\:InternalClaimID[1]/@value={cl_0}

    public static List<FuckingTestCase> generate() {

        List<FuckingTestCase> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.INPUT_FILE)))) {
            int[] positions = new int[0];
            String s;
            FuckingTestCase testCase = null;
            while ((s = reader.readLine()) != null) {
                if ((s = s.trim()).equals("") || s.startsWith("!") || s.startsWith("#")) continue;
                if (s.startsWith("*")) {
                    positions = new int[0];
                    if (testCase != null) list.add(testCase);
                    testCase = new FuckingTestCase();
                    String[] first = s.split(",");
                    testCase.setErrorID(first[0].substring(1));
                    testCase.setClaimTypes(first[1]);
                } else if (s.matches("[ +-,]*[+-]+[ ,+-]*")) {
                    s = s.replaceAll("[ ,]", "");
                    positions = new int[s.length()];
                    int active = testCase.scenarioList.length;
                    for (int i = 0; i < positions.length; i++) {
                        positions[i] = active++;
                    }
                    testCase.addScenarioList(s);
                } else {
                    Map<String, String> macro = new HashMap<>();
                    macro.put(s.substring(0, s.indexOf("=")).trim(), s.substring(s.indexOf("=") + 1).replaceAll("^\\s+", ""));
                    testCase.addScenarioListMap(positions, macro);
                }
            }
            if (testCase != null) list.add(testCase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
