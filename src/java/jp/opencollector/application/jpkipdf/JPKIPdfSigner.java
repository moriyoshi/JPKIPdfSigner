package jp.opencollector.application.jpkipdf;
import com.itextpdf.text.pdf.PdfReader;
import java.io.FileOutputStream;
import com.itextpdf.text.pdf.JPKIWrapper;
import com.itextpdf.text.pdf.JPKIPdfStamper;
import com.itextpdf.text.pdf.JPKIPdfSignatureAppearance;

public class JPKIPdfSigner {
    public static void main(String[] args) throws Exception {
        JPKIWrapper jpki = new JPKIWrapper();
        PdfReader reader = new PdfReader(args[0]);
        FileOutputStream fo = new FileOutputStream(args[1]);
        JPKIPdfStamper stamper = JPKIPdfStamper.createSignature(reader, fo, '\0');
        JPKIPdfSignatureAppearance sa = stamper.getSignatureAppearance();
        sa.setCrypto(jpki, JPKIPdfSignatureAppearance.WINCER_SIGNED);
        stamper.close();
    }
}
