package csv_ecf_map.utils.CSVutility;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by c-sergluca on 10/26/2018.
 */
public class CreateUnicValuesCSV {

    static List<String> ignoreFieldsPerLine = Arrays.asList(
            "EMCSV", "1.5.3", "BatchID", "Medical", "S-KS Source", "S-KS", "00005", "False", "True",
            "142016053000", "1420160530001", "1420160530001", "1420160530002", "573",
            "123000000100", "14000000012300", "1230000001001", "140000000123001",
            "D8", "RD8", "D9", "9A", "XX", "G2", "LU",
            "BK", "ABK", "ABJ", "BBR", "BR", "BE", "0F", "0B", "D2150", "D0120", "MC", "MI", "MD"
    );
    static int dates = 10;
    static int npis = 10;
    static char unitar = 'A';
    static char unitar1 = 'A';
    static int hren = 1;
    static int hren1 = 1;
    static int others = 1;
    static Set<String> set = new HashSet<>();

    public static void main(String[] args) {
        generate("D:\\1_Project\\Templates\\CSV_Dent.dat");//_generated
        generate("D:\\1_Project\\Templates\\CSV_Inst.dat");//_generated
        generate("D:\\1_Project\\Templates\\CSV_Prof.dat");//_generated
    }

    public static void generate(String path) {
        dates = 10;
        npis = 10;
        unitar = 'A';
        unitar1 = 'A';
        hren = 1;
        hren1 = 1;
        others = 1;
        set = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
             PrintWriter writer = new PrintWriter(path.replace(".", "_generated."));
        ) {
            Set<String> repetat = new HashSet<>();
            StringBuilder sb = new StringBuilder();

            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("00") || s.startsWith("01")) {
                    writer.println(s);
                    continue;
                }
                sb.setLength(0);
                List<String> list = Arrays.asList(s.split(","));
                boolean first = false;
                for (String s1 : list) {
                    if (!first) {
                        sb.append(s1).append(",");
                        first = true;
                        continue;
                    }
                    if (StringUtils.isNotBlank(s1) && !ignoreFieldsPerLine.contains(s1) && !set.add(s1)) {
                        sb.append(change(s1)).append(",");
                        repetat.add(s1);
                    } else sb.append(s1).append(",");
                }
                writer.println(sb.substring(0, sb.length() - 1));
            }
            repetat.stream().forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String change(String s) {
        if (s.contains("2016")) s = "201601" + (dates++);
        if (s.contains("2017")) s = "201701" + (dates++);
        else if (s.matches("[0-9A-Z]{10}")) s = s.substring(0, 9) + (npis++);
        else if (s.matches("[-+1-9]{1}[0-9]*")) {
            System.out.println(s);
            s = Integer.valueOf(s) + (hren1++) + "";
            while (!set.add(s) || ignoreFieldsPerLine.contains(s)) s = Integer.valueOf(s) + (hren1++) + "";
            System.out.println(s);
        } else if (s.length() == 1 && Character.isAlphabetic(s.charAt(0))) {
            s = String.valueOf(unitar++);
            if (s.length() == 1 && !Character.isAlphabetic(s.charAt(0))) {
                s = "AA";
                unitar = 'A';
            }
            while (!set.add(s) || ignoreFieldsPerLine.contains(s)) {
                if (s.length() == 1 && Character.isAlphabetic(s.charAt(0))) {
                    s = String.valueOf(unitar++);
                    if (!Character.isAlphabetic(s.charAt(0))) s = "AA";
                } else {
                    s = "A" + (unitar1++);
                }
            }
        } else if (s.length() == 2) s = s + (hren++);
        else s = s.substring(0, s.length() - 1) + (others++);
        return s;
    }
}
