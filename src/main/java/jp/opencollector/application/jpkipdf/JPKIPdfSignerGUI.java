/*
 * Copyright (c) 2011 Moriyoshi Koizumi
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * you must retain the producer line in every PDF that is created or manipulated
 * using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package jp.opencollector.application.jpkipdf;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import jp.go.jpki.appli.JPKICryptJNIException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.pdf.JPKIPdfSignatureAppearance;
import com.itextpdf.text.pdf.JPKIPdfStamper;
import com.itextpdf.text.pdf.JPKIWrapper;
import com.itextpdf.text.pdf.JPKIWrapperException;
import com.itextpdf.text.pdf.PdfReader;


public class JPKIPdfSignerGUI {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("jp.opencollector.application.jpkipdf.messages"); //$NON-NLS-1$
    private static final String[] EXTENSIONS = { "pdf" };
    protected Shell shlJpkiPdfSigner;
    private Text textInputFile;
    private Text textOutputFile;
    private Text textMessages;

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args) {
        try {
            JPKIPdfSignerGUI window = new JPKIPdfSignerGUI();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    public void open() {
        Display display = Display.getDefault();
        createContents();
        shlJpkiPdfSigner.open();
        shlJpkiPdfSigner.layout();
        while (!shlJpkiPdfSigner.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the window.
     */
    protected void createContents() {
        shlJpkiPdfSigner = new Shell();
        shlJpkiPdfSigner.setSize(456, 279);
        shlJpkiPdfSigner.setText(BUNDLE.getString("JPKIPdfSignerGUI.shlJpkiPdfSigner.text"));
        shlJpkiPdfSigner.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Composite composite_2 = new Composite(shlJpkiPdfSigner, SWT.NONE);
        composite_2.setLayout(new FormLayout());
        
        Label lblInputFile = new Label(composite_2, SWT.NONE);
        FormData fd_lblInputFile = new FormData();
        fd_lblInputFile.left = new FormAttachment(0, 10);
        fd_lblInputFile.top = new FormAttachment(0, 13);
        lblInputFile.setLayoutData(fd_lblInputFile);
        lblInputFile.setText(BUNDLE.getString("JPKIPdfSignerGUI.lblInputFile.text"));
        
        Label lblOutputFile = new Label(composite_2, SWT.NONE);
        FormData fd_lblOutputFile = new FormData();
        fd_lblOutputFile.top = new FormAttachment(lblInputFile, 18);
        fd_lblOutputFile.left = new FormAttachment(0, 10);
        lblOutputFile.setLayoutData(fd_lblOutputFile);
        lblOutputFile.setText(BUNDLE.getString("JPKIPdfSignerGUI.lblOutputFile.text"));

        Button btnInputFile = new Button(composite_2, SWT.NONE);
        btnInputFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                FileDialog dialog = new FileDialog(shlJpkiPdfSigner, SWT.OPEN);
                dialog.setFilterExtensions(EXTENSIONS);
                dialog.open();
                final String fileName = dialog.getFileName();
                if (fileName != null && fileName.length() > 0) {
                    final String parentDirectory = dialog.getFilterPath();
                    textInputFile.setText(parentDirectory + java.io.File.separatorChar + fileName);
                    if (textOutputFile.getText().length() == 0) {
                        final int i = fileName.lastIndexOf('.');
                        final String outputFileName = i >= 0 ?
                                fileName.substring(0, i) + ".signed"
                                + fileName.substring(i):
                                fileName + ".signed";
                        textOutputFile.setText(parentDirectory + java.io.File.separatorChar + outputFileName);
                    }
                }
            }
        });
        FormData fd_btnInputFile = new FormData();
        fd_btnInputFile.top = new FormAttachment(0, 5);
        fd_btnInputFile.left = new FormAttachment(100, -49);
        fd_btnInputFile.right = new FormAttachment(100, -10);
        btnInputFile.setLayoutData(fd_btnInputFile);
        btnInputFile.setText("...");

        Button btnOutputFile = new Button(composite_2, SWT.NONE);
        btnOutputFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shlJpkiPdfSigner, SWT.OPEN);
                dialog.open();
                final String fileName = dialog.getFileName();
                if (fileName != null && fileName.length() > 0) {
                    textOutputFile.setText(dialog.getFilterPath() + java.io.File.separatorChar + fileName);
                }
            }
        });
        FormData fd_btnOutputFile = new FormData();
        fd_btnOutputFile.top = new FormAttachment(btnInputFile, 2);
        fd_btnOutputFile.left = new FormAttachment(100, -49);
        fd_btnOutputFile.right = new FormAttachment(100, -10);
        btnOutputFile.setLayoutData(fd_btnOutputFile);
        btnOutputFile.setText("...");
        
        textInputFile = new Text(composite_2, SWT.BORDER);
        FormData fd_textInputFile = new FormData();
        fd_textInputFile.left = new FormAttachment(0, 107);
        fd_textInputFile.right = new FormAttachment(btnInputFile, -2);
        fd_textInputFile.top = new FormAttachment(0, 10);
        textInputFile.setLayoutData(fd_textInputFile);
        
        textOutputFile = new Text(composite_2, SWT.BORDER);
        fd_lblOutputFile.right = new FormAttachment(100, -355);
        FormData fd_textOutputFile = new FormData();
        fd_textOutputFile.left = new FormAttachment(0, 107);
        fd_textOutputFile.right = new FormAttachment(btnOutputFile);
        fd_textOutputFile.top = new FormAttachment(0, 42);
        textOutputFile.setLayoutData(fd_textOutputFile);
        
        SashForm sashForm = new SashForm(composite_2, SWT.NONE);
        FormData fd_sashForm = new FormData();
        fd_sashForm.bottom = new FormAttachment(100, -10);
        fd_sashForm.right = new FormAttachment(100, -10);
        fd_sashForm.left = new FormAttachment(0, 10, 10);
        fd_sashForm.height = 30;
        sashForm.setLayoutData(fd_sashForm);

        new Composite(sashForm, SWT.NONE);

        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL));

        Button btnSignIt = new Button(composite, SWT.NONE);
        btnSignIt.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
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
                            shlJpkiPdfSigner.getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    java.io.StringWriter writer = new java.io.StringWriter();
                                    e.printStackTrace(new java.io.PrintWriter(writer));
                                    textMessages.insert(writer.toString());
                                    textMessages.insert("\n");
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
        });
        btnSignIt.setText(BUNDLE.getString("JPKIPdfSignerGUI.btnSignIt.text"));
        Button btnCancel = new Button(composite, SWT.NONE);
        btnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                shlJpkiPdfSigner.close();
            }
        });
        btnCancel.setText(BUNDLE.getString("JPKIPdfSignerGUI.btnCancel.text"));

        new Composite(sashForm, SWT.NONE);

        sashForm.setWeights(new int[] { 1, 2, 1 });

        textMessages = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
        FormData fd_textMessages = new FormData();
        fd_textMessages.right = new FormAttachment(btnInputFile, 0, SWT.RIGHT);
        fd_textMessages.bottom = new FormAttachment(sashForm, -6);
        fd_textMessages.top = new FormAttachment(btnOutputFile, 6);
        fd_textMessages.left = new FormAttachment(0, 10);
        textMessages.setLayoutData(fd_textMessages);
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
        shlJpkiPdfSigner.getDisplay().asyncExec(new Runnable() {
            public void run() {
                textMessages.insert(title + ": " + message + "\n");
                MessageBox msgBox = new MessageBox(shlJpkiPdfSigner);
                msgBox.setText(title);
                msgBox.setMessage(message);
                msgBox.open();
            }
        });
    }
}
