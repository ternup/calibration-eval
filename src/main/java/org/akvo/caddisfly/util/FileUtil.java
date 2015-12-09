package org.akvo.caddisfly.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by super on 2/12/15.
 */
public class FileUtil {

    /**
     * Load lines of strings from a file
     *
     * @param path     the path to the file
     * @param fileName the file name
     * @return an list of string lines
     */
    public static ArrayList<String> loadFromFile(File path, String fileName) {

        try {
            ArrayList<String> arrayList = new ArrayList<>();
            if (path.exists()) {

                File file = new File(path, fileName);

                FileReader filereader = new FileReader(file);

                BufferedReader in = new BufferedReader(filereader);
                String line;
                while ((line = in.readLine()) != null) {
                    arrayList.add(line);
                }
                in.close();
                filereader.close();
            }
            return arrayList;
        } catch (Exception ignored) {
        }

        return null;
    }
}
