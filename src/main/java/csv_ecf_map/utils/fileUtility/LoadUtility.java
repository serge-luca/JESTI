package csv_ecf_map.utils.fileUtility;

import java.io.*;
import java.util.*;

/**
 * Created by c-sergluca on 3/19/2018.
 */

class A{
    String hz ="class A";
}

class B extends A {
    String hz = "class B";
}

public class LoadUtility {
public static void main (String[] args){
    A test = new B();
    B curatB = new B();
    System.out.println(test.hz);
    System.out.println(curatB.hz);
}
    public static StringBuilder getContentSBfromFile(File fileName){
        StringBuilder stringBuilder = new StringBuilder();
        try(BufferedReader file = new BufferedReader(new FileReader(fileName));){
            String line;
            while ((line = file.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append('\r').append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    /**
        Return array of [HeaderLine, String content, FooterLine]
     */
    public static String[] getContentHeaderFooterfromFile(File fileName){
        String[] result = {"","",""};
        boolean firstLine = false;
        StringBuilder sb = new StringBuilder();
        try(BufferedReader file = new BufferedReader(new FileReader(fileName));){
            String line;
            while ((line = file.readLine()) != null) {
                if(!firstLine) {
                    firstLine=true;
                    result[0]=line;                                                     //add header
                    continue;
                }
                sb.append(line).append("\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(sb.length()>0){
            sb.replace(sb.lastIndexOf("\r\n"),sb.length(),"");
            if(sb.lastIndexOf("\r\n")>0){
                result[2]=sb.substring(sb.lastIndexOf("\r\n")+2, sb.length());
                sb.replace(sb.lastIndexOf("\r\n")+2,sb.length(),"");                      //add footer
            }
            result[1]=sb.toString();                                                    //add contend
        }

        return result;
    }

    public static Map<String, List<String[]>> loadfromFile(String fileWithKeys){
        List<String[]> list = new ArrayList<>();
        Map<String, List<String[]>> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileWithKeys))) {
            String line;
            while ((line = br.readLine()) != null) {
                String first = line.substring(0, line.indexOf("="));
                if(map.get(first)==null){
                    List<String[]> list1 = new ArrayList<>();
                    list1.add(line.substring(line.indexOf("=")+1).split("="));
                    map.put(first, list1);
                } else{
                    map.get(first).add(line.substring(line.indexOf("=")+1).split("="));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> getMapFromFile(String fileName) throws IOException {
        Properties prp = loadFromProperties(fileName);
        Map<String,String> map = new HashMap<>();
        for(Object o: prp.keySet()){
            map.put(o.toString(),prp.get(o).toString());
        }
        return map;
    }

    public static Properties loadFromProperties(String fileName) throws IOException {
        Properties propFile = new Properties();
        propFile.load(new BufferedInputStream(new FileInputStream(fileName)));
        return  propFile;
    }
}
