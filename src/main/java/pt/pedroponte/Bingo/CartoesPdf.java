package pt.pedroponte.Bingo;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/** Gera um PDF A4 com 6 cartões por folha (2 x 3), prontos a recortar. */
public final class CartoesPdf {

    private static final Font TITULO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font LETRA = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
    private static final Font NUMERO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font LIVRE = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Color AZUL = new Color(0x2c, 0x8d, 0xce);

    private CartoesPdf() {}

    public static void gerar(List<List<List<String>>> cartoes, String sessao, Path destino) throws IOException {
        Document doc = new Document(PageSize.A4, 24, 24, 24, 24);
        try (FileOutputStream out = new FileOutputStream(destino.toFile())) {
            PdfWriter.getInstance(doc, out);
            doc.open();
            PdfPTable folha = new PdfPTable(2);
            folha.setWidthPercentage(100);
            for (int i = 0; i < cartoes.size(); i++) {
                PdfPCell celula = new PdfPCell(cartao(cartoes.get(i), i + 1, sessao));
                celula.setPadding(10);
                celula.setBorder(Rectangle.BOX);
                celula.setBorderColor(Color.LIGHT_GRAY); // linha de corte
                celula.setFixedHeight(258); // 3 cartões por coluna numa A4
                folha.addCell(celula);
            }
            folha.completeRow(); // preenche a última linha se o número for ímpar
            doc.add(folha);
            doc.close();
        }
    }

    private static PdfPTable cartao(List<List<String>> grade, int numero, String sessao) {
        PdfPTable t = new PdfPTable(5);
        t.setWidthPercentage(100);

        PdfPCell titulo = new PdfPCell(new Phrase("Cartão Nº " + numero + "   ·   " + sessao, TITULO));
        titulo.setColspan(5);
        titulo.setBorder(Rectangle.NO_BORDER);
        titulo.setPaddingBottom(6);
        t.addCell(titulo);

        for (char letra : "BINGO".toCharArray()) {
            PdfPCell c = celula(new Phrase(String.valueOf(letra), LETRA));
            c.setBackgroundColor(AZUL);
            t.addCell(c);
        }
        for (List<String> linha : grade) {
            for (String n : linha) {
                boolean livre = n.equals("Livre");
                PdfPCell c = celula(new Phrase(livre ? "LIVRE" : n, livre ? LIVRE : NUMERO));
                if (livre) c.setBackgroundColor(new Color(0xee, 0xee, 0xee));
                t.addCell(c);
            }
        }
        return t;
    }

    private static PdfPCell celula(Phrase p) {
        PdfPCell c = new PdfPCell(p);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setFixedHeight(34);
        return c;
    }
}
