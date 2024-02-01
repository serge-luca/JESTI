package csv_ecf_map.process;

import csv_ecf_map.utils.fileUtility.LoadUtility;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static csv_ecf_map.Constants.*;
import static csv_ecf_map.process.UserController.processPerClaim;

/**
 * Created by c-sergluca on 10/18/2018.
 */
public class MacrosCSVUtility {
    //*************Pseudo-Cache***********************************************************
    private static Map<String,String[]> contentsMap = new HashMap<>();
    private static Map<String,Map<String, String>> macros_to_ECFMap = new HashMap<>();
    private static Map<String,Map<String, String>> ecf_to_MacrosMap = new HashMap<>();
    private static Map<String,Map<String, String>> ecf_to_valuesMap = new HashMap<>();
    private static Map<String,Map<String, String>> customecf_to_valuesMap = new HashMap<>();

    public static Map<String, String> getMacros_to_ECFMap(String  claimType) throws IOException {
        macros_to_ECFMap.putIfAbsent(claimType,loadProps(MACROS_ECFFile_MODEL.replace(".properties", "_"+claimType+".properties")));
        return macros_to_ECFMap.get(claimType);
    }
    public static Map<String, String> getECF_to_MacrosMap(String  claimType) {
        if(!ecf_to_MacrosMap.containsKey(claimType)){
            Map<String,String> inversMap = new HashMap<>();
            try {
                getMacros_to_ECFMap(claimType).forEach((a,b)->inversMap.put(b,a));
            } catch (IOException e) {
                e.printStackTrace();
            }
            ecf_to_MacrosMap.put(claimType,inversMap);
        }
        return ecf_to_MacrosMap.get(claimType);
    }
    public static Map<String, String> getEcf_to_valuesMap(String  claimType) throws IOException {
        ecf_to_valuesMap.putIfAbsent(claimType,loadProps(ECF_VALUESFile_MODEL.replace(".properties", "_"+claimType+".properties")));
        return ecf_to_valuesMap.get(claimType);
    }
    public static Map<String, String> getCustomEcf_to_valuesMap(String  claimType) throws IOException {
        customecf_to_valuesMap.putIfAbsent(claimType,loadProps(ECF_VALUEScustom_MODEL.replace(".properties", "_"+claimType+".properties")));
        return customecf_to_valuesMap.get(claimType);
    }

    public static Map<String,String> loadProps(String path) throws IOException {
        Map<String,String> map = new HashMap<>();
        LoadUtility.getMapFromFile(path).forEach((k,v)->map.put(formatString(k),formatString(v)));
        return map;
    }

    public static String formatString(String s){
        if(!s.matches(".*\\{.*\\}.*")) s= s.replace(":","\\:");

        return s;
    }

    public static String[] getContentArray(File fileName){
        if(contentsMap.get(fileName.getAbsolutePath())==null){
            String[] headerAndFooter = LoadUtility.getContentHeaderFooterfromFile(fileName);
            contentsMap.put(fileName.getAbsolutePath(),headerAndFooter);
            return headerAndFooter;
        }else return contentsMap.get(fileName.getAbsolutePath());
    }
    //*************************************************************************************

    public static void generate_macros_CSVs(){
        if(!generateDefaultMacrosTOecfFiles){
            System.out.println("<<Macros>>-to-<<ECF paths>> and <<ECF paths>>-to-<<Values>> creation disabled");
        }
        if(!generateCSV_with_Macros){
            System.out.println("CSV with Macros creation disabled");
        }
        if(!generateDefaultMacrosTOecfFiles && !generateCSV_with_Macros) return;
        File templates = new File(TEMPLATES_FOLDER);
        if(templates.exists() && templates.isDirectory()){
            List<File> files = Arrays.asList(templates.listFiles());
            Map<String,List<File>> map = files.stream()
                    .filter(i->i.getName().matches(".*[_]+.*[.]{1}.*"))
                    .collect(Collectors.groupingBy(i->{String name =i.getName(); return name.substring(name.lastIndexOf("_")+1, name.lastIndexOf("."));}));

            for(ClaimType clmType: ClaimType.values()){
                String claimType = clmType.name();
                processPerClaim(map.get(claimType), claimType);
            }
        }else System.out.println(templates.getAbsolutePath() +" folder - is not present");
    }


    public static StringBuilder replaceVAlues_with_Macros(File fileName,String claimType) {
        String[] headerAndFooter = getContentArray(fileName);
        List<String> content = getCSV_MacrosList(headerAndFooter,claimType);

        //Recreate content with macros
        return new StringBuilder(headerAndFooter[0])
                .append(delimiter)
                .append(content.stream().collect(Collectors.joining(fieldDelimiter)))
                .append(headerAndFooter[2]);
    }

    public static List<String> getCSV_MacrosList(String[] sb, String claimType) {
        List<String> list = new ArrayList<>(Arrays.asList(sb[1].split(fieldDelimiter)));

        Map<String, String> valuesToMacros = getValuesToMacros(claimType,false);

        for (int i = 1; i < list.size(); i++) {
            boolean hasEOL = false;
            String temp = list.get(i);
            String temp1 = temp;
            if (temp1.indexOf(delimiter) > 0) {
                hasEOL = true;
                temp1 = temp1.substring(0, temp1.indexOf(delimiter));
            }

            if ((temp1 = valuesToMacros.get(temp1)) != null) {
                if (temp1.contains("}{")) temp1 = "?" + temp1;
                if (hasEOL)
                    list.set(i, temp1 + temp.substring(temp.indexOf(delimiter)));
                else list.set(i, temp1);
            }
        }
        System.out.println("Multiple macros matches are marked with \"?\" and you have to check " + list.stream().filter(i -> i.startsWith("?{")).count() + " fields");

        return list;
    }

    public static Map<String, String> getValuesToMacros(String claimType, boolean testGeneration) {

            Map<String, String> macros_to_Values_prop = getMacrosToValuesPop(claimType, testGeneration);
            return macros_to_Values_prop.keySet().stream().collect(Collectors.toMap(macros_to_Values_prop::get, k -> k, (x, y) -> x + y));

    }

    public static Map<String,String> getMacrosToValuesPop(String claimType, boolean testGeneration) {
        try {
            Map<String, String> macros_to_ECF_prop = getMacros_to_ECFMap(claimType);
            Map<String, String> ecf_toValues_prop = getEcf_to_valuesMap(claimType);
            if(testGeneration){
                //****************** check custom values of ECF*********************************************************
                String absolutePathfile = ECF_VALUEScustom_MODEL.replace(".properties","_"+claimType+".properties");
                if(new File(absolutePathfile).exists()){
                    Map<String, String> macros_to_ValuesCustom_prop = getCustomEcf_to_valuesMap(claimType);
                    ecf_toValues_prop.putAll(macros_to_ValuesCustom_prop);
                }
                //******************************************************************************************************
            }
            return macros_to_ECF_prop.keySet().
                    //stream().filter(key->ecf_toValues_prop.containsKey(macros_to_ECF_prop.get(key))).collect(Collectors.toMap(i -> i, k -> {String s = macros_to_ECF_prop.get(k);
                    stream().collect(Collectors.toMap(i -> i, k -> {String s = macros_to_ECF_prop.get(k);
                return ecf_toValues_prop.get(s)!=null ? ecf_toValues_prop.get(s): "";
            },(x, y) -> x + y));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
