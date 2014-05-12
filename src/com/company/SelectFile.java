package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by Gamma on 4/24/2014.
 */
class SelectFile extends JFileChooser {

    private static final SelectFile sf = new SelectFile();

    public static File selectFile(String s) {
        return sf.selectFileUsingSwingJFileChooser(s);
    }

    public static File selectFolder(String s) {
        return sf.selectFolderUsingSwingJFileChooser(s);
    }


    File selectFileUsingSwingJFileChooser(String s) {

        setDialogTitle(s);
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        File file;

        try {

            int approval = showOpenDialog(this);
            file = getSelectedFile();

            if (file != null && file.exists() && file.canRead() && approval == JFileChooser.APPROVE_OPTION) {
                String name = file.getAbsolutePath();
                System.out.println("Loading file " + name);
                return file;
            } else {
                return null;
            }

        } catch (HeadlessException E) {
            System.err.println("Could not open file.");
            JOptionPane.showMessageDialog(this, "Could not open file.", "fuck!", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
            return null;
        }
    }


    File selectFolderUsingSwingJFileChooser(String s) {

        setDialogTitle(s);
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setAcceptAllFileFilterUsed(false);
        File file;

        try {
            int approval = showDialog(this, "save to folder");
            file = getSelectedFile();

            if (file != null && file.exists() && file.canRead() && approval == JFileChooser.APPROVE_OPTION) {
                String name = file.getAbsolutePath();
                System.out.println("Opening folder " + name);
                return file;
            } else {
                return null;
            }

        } catch (HeadlessException E) {
            System.err.println("Could not open file.");
            JOptionPane.showMessageDialog(this, "Could not open folder for writing.", "fuck!", JOptionPane.ERROR_MESSAGE);
            E.printStackTrace();
            return null;
        }
    }


}
