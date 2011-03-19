package jp.opencollector.application.jpkipdf;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JButton;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.pdf.JPKIPdfSignatureAppearance;
import com.itextpdf.text.pdf.JPKIPdfStamper;
import com.itextpdf.text.pdf.JPKIWrapper;
import com.itextpdf.text.pdf.JPKIWrapperException;
import com.itextpdf.text.pdf.PdfReader;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.go.jpki.appli.JPKICryptJNIException;

public class JPKIPdfSignerSwingGUI {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("jp.opencollector.application.jpkipdf.messages"); //$NON-NLS-1$

    private JFrame frame;
    private JTextField textInputFile;
    private JTextField textOutputFile;
    private JTextArea textMessages;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", BUNDLE.getString("JPKIPdfSignerGUI.shlJpkiPdfSigner.text"));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JPKIPdfSignerSwingGUI window = new JPKIPdfSignerSwingGUI();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public JPKIPdfSignerSwingGUI() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle(BUNDLE.getString("JPKIPdfSignerGUI.shlJpkiPdfSigner.text")); //$NON-NLS-1$
        frame.getContentPane().setEnabled(false);
        frame.setBounds(100, 100, 450, 221);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SpringLayout springLayout = new SpringLayout();
        frame.getContentPane().setLayout(springLayout);
        
        textInputFile = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, textInputFile, 4, SpringLayout.NORTH, frame.getContentPane());
        frame.getContentPane().add(textInputFile);
        textInputFile.setColumns(10);
        
        JLabel lblInputFile = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblInputFile.text"));
        springLayout.putConstraint(SpringLayout.WEST, textInputFile, 6, SpringLayout.EAST, lblInputFile);
        springLayout.putConstraint(SpringLayout.EAST, lblInputFile, 104, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblInputFile, 10, SpringLayout.WEST, frame.getContentPane());
        lblInputFile.setLabelFor(textInputFile);
        springLayout.putConstraint(SpringLayout.NORTH, lblInputFile, 10, SpringLayout.NORTH, frame.getContentPane());
        frame.getContentPane().add(lblInputFile);
        
        JButton btnInputFile = new JButton("...");
        btnInputFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnInputFile_clicked();
            }
        });
        springLayout.putConstraint(SpringLayout.EAST, textInputFile, 0, SpringLayout.WEST, btnInputFile);
        springLayout.putConstraint(SpringLayout.WEST, btnInputFile, -48, SpringLayout.EAST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, btnInputFile, 0, SpringLayout.NORTH, textInputFile);
        springLayout.putConstraint(SpringLayout.EAST, btnInputFile, -6, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(btnInputFile);
        
        JLabel lblOutputFile = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblOutputFile.text")); //$NON-NLS-1$
        springLayout.putConstraint(SpringLayout.NORTH, lblOutputFile, 48, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblOutputFile, 10, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, lblOutputFile, 104, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(lblOutputFile);
        
        textOutputFile = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, textOutputFile, -6, SpringLayout.NORTH, lblOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, textOutputFile, 6, SpringLayout.EAST, lblOutputFile);
        lblOutputFile.setLabelFor(textOutputFile);
        textOutputFile.setColumns(10);
        frame.getContentPane().add(textOutputFile);
        
        JButton btnOutputFile = new JButton("...");
        btnOutputFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnOutputFile_clicked();
            }
        });
        springLayout.putConstraint(SpringLayout.EAST, textOutputFile, 0, SpringLayout.WEST, btnOutputFile);
        springLayout.putConstraint(SpringLayout.NORTH, btnOutputFile, 0, SpringLayout.NORTH, textOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, btnOutputFile, -48, SpringLayout.EAST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, btnOutputFile, -6, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(btnOutputFile);
        
        JPanel panel = new JPanel();
        springLayout.putConstraint(SpringLayout.NORTH, panel, -40, SpringLayout.SOUTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, panel, 6, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.SOUTH, panel, -8, SpringLayout.SOUTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, panel, -6, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(panel);
        
        JButton btnSignIt = new JButton(BUNDLE.getString("JPKIPdfSignerGUI.btnSignIt.text")); //$NON-NLS-1$
        btnSignIt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnSignIt_clicked();
            }
        });
        panel.add(btnSignIt);
        
        JButton btnClose = new JButton(BUNDLE.getString("JPKIPdfSignerGUI.btnClose.text")); //$NON-NLS-1$
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnClose_clicked();
            }
        });
        panel.add(btnClose);
        
        textMessages = new JTextArea();
        JScrollPane textMessagesScrollPane = new JScrollPane(textMessages);
        springLayout.putConstraint(SpringLayout.NORTH, textMessagesScrollPane, 12, SpringLayout.SOUTH, btnOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, textMessagesScrollPane, 10, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.SOUTH, textMessagesScrollPane, -6, SpringLayout.NORTH, panel);
        springLayout.putConstraint(SpringLayout.EAST, textMessagesScrollPane, -10, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(textMessagesScrollPane);
    }

    private void btnInputFile_clicked() {
        JFileChooser dialog = new JFileChooser();
        dialog.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (!f.isFile())
                    return false;
                final String name = f.getName();
                final int pos = name.lastIndexOf('.');
                return pos >= 0 && name.substring(pos).toLowerCase().equals(".pdf");
            }
            public String getDescription() {
                return BUNDLE.getString("JPKIPdfSignerGUI.pdfDocument") + " (.pdf)";
            }
        });
        if (dialog.showOpenDialog(frame) == JFileChooser.CANCEL_OPTION)
            return;
        final File file = dialog.getSelectedFile();
        final String fileName = file.getName();
        textInputFile.setText(file.getAbsolutePath());
        if (textOutputFile.getText().length() == 0) {
            final int i = fileName.lastIndexOf('.');
            final String outputFileName = i >= 0 ?
                    fileName.substring(0, i) + ".signed"
                    + fileName.substring(i):
                    fileName + ".signed";
            textOutputFile.setText(new File(file.getParent(), outputFileName).getAbsolutePath());
        }
    }

    private void btnOutputFile_clicked() {
        JFileChooser dialog = new JFileChooser();
        if (dialog.showSaveDialog(frame) == JFileChooser.CANCEL_OPTION)
            return;
        final File file = dialog.getSelectedFile();
        textOutputFile.setText(file.getAbsolutePath());
    }

    private void btnSignIt_clicked() {
        final File inputFile = new File(textInputFile.getText());
        if (!inputFile.exists()) {
            putErrorDialog(new MessageFormat(BUNDLE.getString("JPKIPdfSignerGUI.fileNotFound")).format(new Object[] { inputFile.getPath() }));
            return;
        }
        final File outputFile = new File(textOutputFile.getText());
        new Thread(new Runnable() {
            public void run() {
                try {
                    Throwable cause = null;
                    try {
                        JPKIWrapper jpki = new JPKIWrapper();
                        PdfReader reader = new PdfReader(inputFile.getPath());
                        FileOutputStream fo = new FileOutputStream(outputFile);
                        JPKIPdfStamper stamper = JPKIPdfStamper.createSignature(reader, fo, '\0');
                        JPKIPdfSignatureAppearance sa = stamper.getSignatureAppearance();
                        sa.setCrypto(jpki, JPKIPdfSignatureAppearance.WINCER_SIGNED);
                        stamper.close();
                    } catch (JPKIWrapperException e) {
                        cause = e.getCause();
                    } catch (ExceptionConverter e) {
                        cause = e.getCause().getCause();
                    }
                    if (cause != null) {
                        if (cause instanceof JPKICryptJNIException) {
                            switch (((JPKICryptJNIException)cause).getErrorCode()) {
                            case JPKICryptJNIException.JPKI_ERR_UNKNOWN:
                                putErrorDialog(BUNDLE.getString("JPKIPdfSignerGUI.unknownError"));
                                return;
                            case JPKICryptJNIException.JPKI_ERR_WINDOWS:
                                switch (((JPKICryptJNIException)cause).getWinErrorCode()) {
                                case JPKICryptJNIException.JPKI_WIN_CANCELLED_BY_USER:
                                    putErrorDialog(BUNDLE.getString("JPKIPdfSignerGUI.cancelledByUser"));
                                    break;
                                case JPKICryptJNIException.JPKI_WIN_CHV_BLOCKED:
                                    putErrorDialog(BUNDLE.getString("JPKIPdfSignerGUI.cardLocked"));
                                    break;
                                case JPKICryptJNIException.JPKI_WIN_NOT_READY:
                                    putErrorDialog(BUNDLE.getString("JPKIPdfSignerGUI.cardNotReady"));
                                    break;
                                default:
                                    throw cause;
                                }
                                return;
                            default:
                                throw cause;
                            }
                        } else {
                            throw cause;
                        }
                    }
                } catch (final Throwable e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            java.io.StringWriter writer = new java.io.StringWriter();
                            e.printStackTrace(new java.io.PrintWriter(writer));
                            textMessages.append(writer.toString());
                            textMessages.append("\n");
                            try {
                                writer.close();
                            } catch (IOException e) {}
                        }
                    });
                    return;
                }
                putSuccessDialog(BUNDLE.getString("JPKIPdfSignerGUI.doneSuccessfully"));
            }
        }).start();
    }

    private void btnClose_clicked() {
        frame.dispose();
    }

    private void putSuccessDialog(final String message) {
        putDialog(false, message);
    }

    private void putErrorDialog(final String message) {
        putDialog(true, message);
    }

    private void putDialog(final boolean error, final String message) {
        final String title = error ?
                BUNDLE.getString("JPKIPdfSignerGUI.error"):
                BUNDLE.getString("JPKIPdfSignerGUI.success");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textMessages.append(title + ": " + message + "\n");
                JOptionPane.showMessageDialog(frame, message, title, error ? JOptionPane.ERROR_MESSAGE: JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
