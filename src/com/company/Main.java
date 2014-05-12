package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

final class Main extends JFrame implements Runnable {

    private File in_file;
    private File out_folder;
    private boolean enumerate = false, copying = false, make_script = false;


    public static void main(String[] args) {
        if (args.length > 1) return;
        new Main().execute();
    }

    private void execute() {
        try {
            SwingUtilities.invokeLater(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        /********************************************************/
        //Systeout.println("run() thread runs on EDT? " + SwingUtilities.isEventDispatchThread());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Load an M3U8 or M3U playlist and copy denoted files to new destination");
        setSize(600, 200);
        setLocation(500, 500);
        setLayout(new FlowLayout());
        ImageIcon icon = new ImageIcon("res" + File.separatorChar + "Filetype-m3u-icon.png");
        setIconImage(icon.getImage());

        /********************************************************/

        final JButton jb_load_playlist = new JButton("browse for input M3U8 file");
        final ActionListener al_load_playlist = e -> {
            in_file = SelectFile.selectFile("select M3U/8 playlist file");
        };
        jb_load_playlist.addActionListener(al_load_playlist);

        final JButton jb_set_output_folder = new JButton("select output folder");
        final ActionListener al_set_output_folder = e -> {
            out_folder = SelectFile.selectFolder("select output folder");
        };
        jb_set_output_folder.addActionListener(al_set_output_folder);

        final JButton jb_enumerate_files = new JButton("enumerate files");
        final ActionListener al_enumerate_files = e -> {
            enumerate = !enumerate;
            if (enumerate) jb_enumerate_files.setText("enabled");
            else jb_enumerate_files.setText("disabled");
        };
        jb_enumerate_files.addActionListener(al_enumerate_files);

        final JCheckBox jcb_make_script = new JCheckBox("create batch script", false);
        final ItemListener il_make_script = e->{
            Object source = e.getItemSelectable();
            if (source == jcb_make_script) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    make_script = false;
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    make_script = true;
                }
            }
        };
        jcb_make_script.addItemListener(il_make_script);

        final JButton jb_copy = new JButton("Copy!");
        final ActionListener al_copy = e -> {
            if (in_file != null) {
                if (out_folder != null) {
                    final ArrayList<String> arrayList = fillArrayList(in_file);
                    copying = true;
                    final SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            copy_files(in_file, out_folder, arrayList, enumerate);
                            return null;
                        }
                    };
                    swingWorker.execute(); // not on edt, required to run the later swing worker in copy_files()
                }
            }
        };
        jb_copy.addActionListener(al_copy);

        /********************************************************/

        add(jb_load_playlist);
        add(jb_set_output_folder);
        add(jb_enumerate_files);
        add(jcb_make_script);
        add(jb_copy);

        pack();
        setVisible(true);

        /********************************************************/

    }

    private ArrayList<String> fillArrayList(File f) {
        return ParseFile.parseM3UFile(f);
    }


    private int copy_files(File in, File out, ArrayList<String> list, boolean enumeration) {
        try {

            final JFrame jf_progress_window = new JFrame();
            final JComponent jc_general = new JPanel(new BorderLayout());
            final JPanel jp_general = new JPanel();
            final JTextArea taskOutput = new JTextArea();
            final JProgressBar progressBar = new JProgressBar();
            final JButton jb_cancel = new JButton();
            final ActionListener al_cancel = e -> copying = false;


            //if (in==null || out==null || list==null) return 1;
            //Systeout.println("copy_files() thread runs on EDT? " + SwingUtilities.isEventDispatchThread());

            int number_of_files = list.size();
            final OpenOption[]    options = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
            final BufferedWriter  writer  = Files.newBufferedWriter(Paths.get("copy_files.bat"), StandardCharsets.UTF_8, options);

            jf_progress_window.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            jf_progress_window.setTitle("copying files");
            jf_progress_window.setBounds(500, 400, 680, 300);

            jc_general.setOpaque(true);
            jc_general.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            taskOutput.setRows(8);
            taskOutput.setColumns(40);
            taskOutput.setMargin(new Insets(5, 5, 5, 5));
            taskOutput.setEditable(true);
            final JScrollPane jsp_message = new JScrollPane(taskOutput);

            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);

            jb_cancel.setText("Cancel!");
            jb_cancel.setActionCommand("cancel");
            jb_cancel.addActionListener(al_cancel);

            jp_general.add(jsp_message, BorderLayout.NORTH);
            jp_general.add(progressBar, BorderLayout.CENTER);
            jp_general.add(jb_cancel, BorderLayout.SOUTH);

            jc_general.add(jp_general);
            jf_progress_window.setContentPane(jc_general);
            //jf_progress_window.pack();
            jf_progress_window.setVisible(true);

            SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int progress;
                    setProgress(0);
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    if (list != null && in != null && out != null) {
                        for (int i = 0; copying && (i < number_of_files); i++) {
                            String fromArrayList = list.get(i);
                            File source = new File(fromArrayList);
                            if (source.canRead()) {
                                taskOutput.append(source.toString() + "\n");
                            } else {
                                taskOutput.append("<html>CANNOT READ<font color='red'>red</font></html> " + source.toString() + "\n");
                            }
                            File dest;
                            if (enumeration) {
                                dest = new File(out.toString() + File.separatorChar + (i + 1) + " " + source.getName());
                            } else {
                                dest = new File(out.toString() + File.separatorChar + source.getName());
                            }
                            if (dest.exists()) {
                                int answer = JOptionPane.showConfirmDialog(null, "file exists, do you want to overwrite?");
                                if (answer == JOptionPane.YES_OPTION) {
                                    CopyFilesToStick.copy_files_using_default_method(source, dest);
                                } else if (answer == JOptionPane.NO_OPTION) {
                                } else if (answer == JOptionPane.CANCEL_OPTION) {
                                    copying = false;
                                }
                            } else {
                                CopyFilesToStick.copy_files_using_default_method(source, dest);
                            }
                            if (make_script) writer.write("copy " + "\"" + source.toString() + "\"" + " " + "\"" + dest.toString() + "\"" + "\n");
                            progress = i;
                            setProgress(100 * (progress + 1) / number_of_files);
                        }
                    }
                    return null;
                }

                @Override
                public void done() {
                    Toolkit.getDefaultToolkit().beep();
                    setCursor(null); //turn off the wait cursor
                    setProgress(100);
                    taskOutput.append("Finished!\n");
                }
            };
            PropertyChangeListener pcl = evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setIndeterminate(false);
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                    //taskOutput.append(String.format("Completed %d%% of task.\n", progress));
                }
            };
            sw.addPropertyChangeListener(pcl);
            sw.run(); // runs or reboots on edt, otherwise execute() doesn't run at all

            if (make_script) writer.flush();
            if (make_script) writer.close();
            copying = false;
            //jf_progress_window.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}



