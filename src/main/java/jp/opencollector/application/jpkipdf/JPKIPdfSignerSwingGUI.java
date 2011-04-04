package jp.opencollector.application.jpkipdf;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JSpinner;
import javax.swing.JButton;

import java.text.MessageFormat;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Rectangle;
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
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;

abstract class DateTimeEntry {
    public void populate() {
        populateYMD();
        populateHMS();
    }

    protected abstract void populateYMD();

    protected abstract void populateHMS();

    public abstract void setCalendar(GregorianCalendar cal);

    public abstract GregorianCalendar getCalendar();

    public DateTimeEntry(JPanel panel, ResourceBundle bundle) {
        this.panel = panel;
        this.bundle = bundle;
    }

    protected JPanel panel;
    protected ResourceBundle bundle;
    protected JSpinner hour;
    protected JSpinner minute;
    protected JSpinner second;
}

class DateTimeEntry_ja extends DateTimeEntry {
    protected void populateYMD() {
        year = new JSpinner();
        year.setEditor(new JSpinner.NumberEditor(year, "0000"));
        year.setModel(new SpinnerNumberModel(2000, 1970, 2100, 1));
        panel.add(year);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.year")));
        month = new JSpinner();
        month.setEditor(new JSpinner.NumberEditor(month, "00"));
        month.setModel(new SpinnerNumberModel(1, 1, 12, 1));
        panel.add(month);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.month")));
        day = new JSpinner();
        day.setEditor(new JSpinner.NumberEditor(day, "00"));
        day.setModel(new SpinnerNumberModel(1, 1, 31, 1));
        panel.add(day);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.day")));
    }

    protected void populateHMS() {
        hour = new JSpinner();
        hour.setEditor(new JSpinner.NumberEditor(hour, "00"));
        hour.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        panel.add(hour);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.hour")));
        minute = new JSpinner();
        minute.setEditor(new JSpinner.NumberEditor(minute, "00"));
        minute.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        panel.add(minute);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.minute")));
        second = new JSpinner();
        second.setEditor(new JSpinner.NumberEditor(second, "00"));
        second.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        panel.add(second);
        panel.add(new JLabel(bundle.getString("JPKIPdfSignerGUI.second")));
    }

    public GregorianCalendar getCalendar() {
        return new GregorianCalendar(
                ((Number)year.getValue()).intValue(),
                ((Number)month.getValue()).intValue() - 1,
                ((Number)day.getValue()).intValue()  ,
                ((Number)hour.getValue()).intValue(),
                ((Number)minute.getValue()).intValue(),
                ((Number)second.getValue()).intValue());
    }

    public void setCalendar(GregorianCalendar cal) {
        year.setValue(cal.get(GregorianCalendar.YEAR));
        month.setValue(cal.get(GregorianCalendar.MONTH) + 1);
        day.setValue(cal.get(GregorianCalendar.DAY_OF_MONTH));
        hour.setValue(cal.get(GregorianCalendar.HOUR_OF_DAY));
        minute.setValue(cal.get(GregorianCalendar.MINUTE));
        second.setValue(cal.get(GregorianCalendar.SECOND));
    }

    public DateTimeEntry_ja(JPanel panel, ResourceBundle bundle) {
        super(panel, bundle);
    }

    private JSpinner year;
    private JSpinner month;
    private JSpinner day;
}

class DateTimeEntry_en extends DateTimeEntry {
    public void populateYMD() {
        day = new JSpinner();
        day.setEditor(new JSpinner.NumberEditor(day, "00"));
        day.setModel(new SpinnerNumberModel(1, 1, 31, 1));
        panel.add(day);

        month = new JComboBox();
        month.setEditable(false);
        month.setModel(new MonthComboBoxModel(new AbstractBundleGetter() {
            public ResourceBundle getResourceBundle() {
                return bundle;
            }
        }));
        panel.add(month);

        year = new JSpinner();
        year.setEditor(new JSpinner.NumberEditor(year, "0000"));
        year.setModel(new SpinnerNumberModel(1980, 1970, 2100, 1));
        panel.add(year);
    }

    protected void populateHMS() {
        ampm = new JComboBox();
        DefaultComboBoxModel ampmModel = new DefaultComboBoxModel();
        ampmModel.addElement(bundle.getString("JPKIPdfSignerGUI.AM"));
        ampmModel.addElement(bundle.getString("JPKIPdfSignerGUI.PM"));
        ampm.setModel(ampmModel);
        hour = new JSpinner();
        hour.setEditor(new JSpinner.NumberEditor(hour, "00"));
        hour.setModel(new SpinnerNumberModel(1, 1, 12, 1));
        panel.add(hour);
        panel.add(new JLabel(":"));
        minute = new JSpinner();
        minute.setEditor(new JSpinner.NumberEditor(minute, "00"));
        minute.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        panel.add(minute);
        panel.add(new JLabel(":"));
        second = new JSpinner();
        second.setEditor(new JSpinner.NumberEditor(second, "00"));
        second.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        panel.add(second);
    }

    public GregorianCalendar getCalendar() {
        return new GregorianCalendar(
                ((Number)year.getValue()).intValue(),
                month.getSelectedIndex(),
                ((Number)day.getValue()).intValue(),
                (((Number)hour.getValue()).intValue() % 12) + ampm.getSelectedIndex() * 12,
                ((Number)minute.getValue()).intValue(),
                ((Number)second.getValue()).intValue());
    }

    public void setCalendar(GregorianCalendar cal) {
        year.setValue(cal.get(GregorianCalendar.YEAR));
        month.setSelectedIndex(cal.get(GregorianCalendar.MONTH));
        day.setValue(cal.get(GregorianCalendar.DAY_OF_MONTH));
        hour.setValue(cal.get(GregorianCalendar.HOUR));
        minute.setValue(cal.get(GregorianCalendar.MINUTE));
        second.setValue(cal.get(GregorianCalendar.SECOND));
    }

    public DateTimeEntry_en(JPanel panel, ResourceBundle bundle) {
        super(panel, bundle);
    }

    private JComboBox ampm;
    private JSpinner year;
    private JComboBox month;
    private JSpinner day;
}

public class JPKIPdfSignerSwingGUI {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("jp.opencollector.application.jpkipdf.messages"); //$NON-NLS-1$

    private JFrame frame;
    private JTextField textInputFile;
    private JTextField textOutputFile;
    private JTextArea textMessages;
    private JPanel panelSignDateTime;
    private DateTimeEntry signDateTimeEntry;
    private SpringLayout springLayout;
    private JButton btnInputFile;
    private JLabel lblDateTime;
    private JLabel lblInputFile;
    private JTextField textSignPlace;
    private JLabel lblSignReason;
    private JTextField textSignReason;

    private JCheckBox chckbxPutVisibleSignature;

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
        populateSignDateTimePanel(panelSignDateTime);
        
        JButton btnSignDateTimeSetToNow = new JButton(BUNDLE.getString("JPKIPdfSignerGUI.btnNewButton.text")); //$NON-NLS-1$
        btnSignDateTimeSetToNow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                signDateTimeEntry.setCalendar(new GregorianCalendar());
            }
        });
        springLayout.putConstraint(SpringLayout.EAST, panelSignDateTime, -6, SpringLayout.WEST, btnSignDateTimeSetToNow);
        springLayout.putConstraint(SpringLayout.NORTH, btnSignDateTimeSetToNow, -6, SpringLayout.NORTH, lblDateTime);
        springLayout.putConstraint(SpringLayout.EAST, btnSignDateTimeSetToNow, 0, SpringLayout.EAST, btnInputFile);
        frame.getContentPane().add(btnSignDateTimeSetToNow);
        
        JLabel lblSignPlace = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblSignPlace.text")); //$NON-NLS-1$
        springLayout.putConstraint(SpringLayout.NORTH, lblSignPlace, 124, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblSignPlace, 10, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(lblSignPlace);
        
        textSignPlace = new JTextField();
        lblSignPlace.setLabelFor(textSignPlace);
        springLayout.putConstraint(SpringLayout.NORTH, textSignPlace, -6, SpringLayout.NORTH, lblSignPlace);
        springLayout.putConstraint(SpringLayout.WEST, textSignPlace, 144, SpringLayout.WEST, lblSignPlace);
        springLayout.putConstraint(SpringLayout.EAST, textSignPlace, -10, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(textSignPlace);
        textSignPlace.setColumns(10);
        
        lblSignReason = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblSignReason.text")); //$NON-NLS-1$
        springLayout.putConstraint(SpringLayout.NORTH, lblSignReason, 162, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblSignReason, 10, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(lblSignReason);
        
        textSignReason = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, textSignReason, -6, SpringLayout.NORTH, lblSignReason);
        springLayout.putConstraint(SpringLayout.WEST, textSignReason, 144, SpringLayout.WEST, lblSignReason);
        springLayout.putConstraint(SpringLayout.EAST, textSignReason, -10, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(textSignReason);
        textSignReason.setColumns(10);
        
        chckbxPutVisibleSignature = new JCheckBox(BUNDLE.getString("JPKIPdfSignerGUI.chckbxPutVisibleSignature.text"));
        chckbxPutVisibleSignature.setSelected(true);
        springLayout.putConstraint(SpringLayout.NORTH, chckbxPutVisibleSignature, 200, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, chckbxPutVisibleSignature, 4, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(chckbxPutVisibleSignature);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setTitle(BUNDLE.getString("JPKIPdfSignerGUI.shlJpkiPdfSigner.text")); //$NON-NLS-1$
        frame.getContentPane().setEnabled(false);
        frame.setBounds(100, 100, 653, 505);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        springLayout = new SpringLayout();
        frame.getContentPane().setLayout(springLayout);
        
        textInputFile = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, textInputFile, 4, SpringLayout.NORTH, frame.getContentPane());
        frame.getContentPane().add(textInputFile);
        textInputFile.setColumns(10);
        
        lblInputFile = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblInputFile.text"));
        springLayout.putConstraint(SpringLayout.WEST, textInputFile, 50, SpringLayout.EAST, lblInputFile);
        springLayout.putConstraint(SpringLayout.EAST, lblInputFile, 104, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblInputFile, 10, SpringLayout.WEST, frame.getContentPane());
        lblInputFile.setLabelFor(textInputFile);
        springLayout.putConstraint(SpringLayout.NORTH, lblInputFile, 10, SpringLayout.NORTH, frame.getContentPane());
        frame.getContentPane().add(lblInputFile);
        
        btnInputFile = new JButton("...");
        springLayout.putConstraint(SpringLayout.NORTH, btnInputFile, -6, SpringLayout.NORTH, lblInputFile);
        springLayout.putConstraint(SpringLayout.EAST, textInputFile, 0, SpringLayout.WEST, btnInputFile);
        btnInputFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnInputFile_clicked();
            }
        });
        springLayout.putConstraint(SpringLayout.WEST, btnInputFile, -48, SpringLayout.EAST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, btnInputFile, -6, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(btnInputFile);
        
        JLabel lblOutputFile = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblOutputFile.text")); //$NON-NLS-1$
        springLayout.putConstraint(SpringLayout.NORTH, lblOutputFile, 48, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblOutputFile, 10, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, lblOutputFile, 104, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(lblOutputFile);
        
        textOutputFile = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, textOutputFile, -6, SpringLayout.NORTH, lblOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, textOutputFile, 144, SpringLayout.WEST, lblOutputFile);
        lblOutputFile.setLabelFor(textOutputFile);
        textOutputFile.setColumns(10);
        frame.getContentPane().add(textOutputFile);
        
        JButton btnOutputFile = new JButton("...");
        springLayout.putConstraint(SpringLayout.NORTH, btnOutputFile, -6, SpringLayout.NORTH, lblOutputFile);
        springLayout.putConstraint(SpringLayout.EAST, textOutputFile, 0, SpringLayout.WEST, btnOutputFile);
        btnOutputFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                btnOutputFile_clicked();
            }
        });
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
        springLayout.putConstraint(SpringLayout.NORTH, textMessagesScrollPane, 187, SpringLayout.SOUTH, btnOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, textMessagesScrollPane, 10, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.SOUTH, textMessagesScrollPane, -6, SpringLayout.NORTH, panel);
        springLayout.putConstraint(SpringLayout.EAST, textMessagesScrollPane, -10, SpringLayout.EAST, frame.getContentPane());
        frame.getContentPane().add(textMessagesScrollPane);
        
        lblDateTime = new JLabel(BUNDLE.getString("JPKIPdfSignerGUI.lblDateTime.text")); //$NON-NLS-1$
        springLayout.putConstraint(SpringLayout.NORTH, lblDateTime, 86, SpringLayout.NORTH, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, lblDateTime, 10, SpringLayout.WEST, frame.getContentPane());
        springLayout.putConstraint(SpringLayout.EAST, lblDateTime, 123, SpringLayout.WEST, frame.getContentPane());
        frame.getContentPane().add(lblDateTime);

        panelSignDateTime = new JPanel();
        springLayout.putConstraint(SpringLayout.NORTH, panelSignDateTime, 10, SpringLayout.SOUTH, textOutputFile);
        springLayout.putConstraint(SpringLayout.WEST, panelSignDateTime, 31, SpringLayout.EAST, lblDateTime);
        springLayout.putConstraint(SpringLayout.SOUTH, panelSignDateTime, 6, SpringLayout.SOUTH, lblDateTime);
        FlowLayout flowLayout = (FlowLayout) panelSignDateTime.getLayout();
        flowLayout.setHgap(0);
        flowLayout.setAlignment(FlowLayout.LEFT);
        flowLayout.setVgap(0);
        panelSignDateTime.setBorder(null);
        frame.getContentPane().add(panelSignDateTime);
    }

    private void populateSignDateTimePanel(JPanel panel) {
        String lang = BUNDLE.getLocale().getLanguage();
        final String country = BUNDLE.getLocale().getCountry();
        String[] combinations = null;
        if (lang.length() == 0)
            lang = "en";
        if (country.length() > 0) {
            combinations = new String[] { lang + "_" + country, lang };
        } else {
            combinations = new String[] { lang };
        }

        Class<?> klass = null;
        final Class<?> mine = getClass();
        for (String loc: combinations) {
            try {
                klass = mine.getClassLoader().loadClass(mine.getPackage().getName() + ".DateTimeEntry_" + loc);
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
        }

        DateTimeEntry entry;
        try {
            entry = (DateTimeEntry)klass.getConstructor(JPanel.class, ResourceBundle.class).newInstance(panelSignDateTime, BUNDLE);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        signDateTimeEntry = entry;
        entry.populate();
        entry.setCalendar(new GregorianCalendar());
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
                        JPKIPdfStamper stamper = JPKIPdfStamper.createSignature(reader, fo, BUNDLE, '\0');
                        JPKIPdfSignatureAppearance sa = stamper.getSignatureAppearance();
                        sa.setSignDate(signDateTimeEntry.getCalendar());
                        sa.setName(jpki.getUserKey().getCertificate().getBasicData().getName());
                        sa.setLocation(textSignPlace.getText());
                        sa.setReason(textSignReason.getText());
                        sa.setCrypto(jpki, JPKIPdfSignatureAppearance.WINCER_SIGNED);
                        final Rectangle pageRect = reader.getPageSize(1);
                        if (chckbxPutVisibleSignature.isSelected())
                            sa.setVisibleSignature(new Rectangle(50, pageRect.getHeight() - 50, 250, pageRect.getHeight() - 150), 1, null);
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
