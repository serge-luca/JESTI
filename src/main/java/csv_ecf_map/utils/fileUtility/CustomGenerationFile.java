package csv_ecf_map.utils.fileUtility;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by c-sergluca on 10/19/2018.
 */
public class CustomGenerationFile {

    public static void createFile(StringBuilder content, String fileAbsolutePath){
        File target = new File(fileAbsolutePath).getParentFile();
        if (!target.exists()) target.mkdirs();

        try(FileOutputStream fileOut = new FileOutputStream(fileAbsolutePath)) {
            fileOut.write(content.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Created content for "+ fileAbsolutePath);
    }

//    public static void createFile(Collection<String> content, String delimiter , String fileAbsolutePath){
//        StringBuilder sb = content.stream().collect(StringBuilder::new, (i,j)-> i.append(j).append(delimiter), StringBuilder::append);
//        createFile(sb,fileAbsolutePath);
//    }
    public static void main(String[] args) throws IOException {
        System.out.println();
        Files.lines(Paths.get("E:\\Work\\NCX12_NCAP_837I_20190605_00002_277CA.dat")).filter(i->i.startsWith("SE*")).forEach(System.out::println);
    }
}
