package com.company;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Gamma on 4/24/2014.
 */
class ParseFile {

    public static ArrayList<String> parseM3UFile(final File file) {

        if (file == null) return null;
        if (!file.canRead()) return null;

        String absolutePath = null;
        String root_of_playlist = null;
        BufferedReader fileReader = null;
        BufferedReader bufferedReader;
        String current_line;
        ArrayList<String> out = new ArrayList<>();

        try {
            absolutePath = file.getParent();
            root_of_playlist = getDrive(file);
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            if (fileReader == null) return out;
            System.out.println("parsing file " + file.getAbsolutePath());
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        final Pattern p = Pattern.compile(".*[a-zA-Z]:.*", Pattern.CASE_INSENSITIVE);

        try {
            bufferedReader = new BufferedReader(fileReader);
            while ((current_line = bufferedReader.readLine()) != null) {
                StringBuilder stringBuilder = new StringBuilder();
                String output_file;
                // url
                if (current_line.startsWith("http")) {
                	out.add(current_line);
                // music file line, not tag line
                } else if (!current_line.contains("#")) {
                    // absolute path line
                    if (p.matcher(current_line).matches()) {
                        stringBuilder.append(current_line);
                        output_file = stringBuilder.toString();
                        output_file = Platform_pathname_check.convert_slashes(output_file);
                        if (output_file != null) {
                            //System.out.println(output_file);
                            out.add(output_file);
                        }
                    } else {
                        // relative path line or file in current folder
                        // file in current folder
                        if (!current_line.startsWith("\\")) {
                            stringBuilder.append(absolutePath);
                            stringBuilder.append(File.separatorChar);
                            stringBuilder.append(current_line);
                            output_file = stringBuilder.toString();
                            output_file = Platform_pathname_check.convert_slashes(output_file);
                            if (output_file != null) {
                                //System.out.println(output_file);
                                out.add(output_file);
                            }
                        }
                        // path on the same drive starts with slash and folder name
                        if (current_line.startsWith("\\")) {
                            stringBuilder.append(root_of_playlist);
                            stringBuilder.append(":");
                            stringBuilder.append(current_line);
                            output_file = stringBuilder.toString();
                            output_file = Platform_pathname_check.convert_slashes(output_file);
                            if (output_file != null) {
                                //System.out.println(output_file);
                                out.add(output_file);
                            }
                        }
                    }
                }
            }

            try {
                fileReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;

    }


    private static String getDrive(File pFile) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            String path = pFile.getAbsolutePath();
            if (path == null) {
                return "";
            } else {
                return path.substring(0, 1);
            }
        } else {
            return "";
        }
    }

}

