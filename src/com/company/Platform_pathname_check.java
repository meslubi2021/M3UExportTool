package com.company;

/**
 * Created by Gamma on 4/29/2014.
 */

import java.io.File;

class Platform_pathname_check {

    public static String convert_slashes(String in) {

        if (in == null) return null;

        try {

            if (File.separatorChar == '\\') {
                in = in.replace('/', File.separatorChar);
                in = in.replace('\\', File.separatorChar);
            }
            if (File.separatorChar == '/') {
                in = in.replace('\\', File.separatorChar);
                in = in.replace('/', File.separatorChar);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return in;

    }

}