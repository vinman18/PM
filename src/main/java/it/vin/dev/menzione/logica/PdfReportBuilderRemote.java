package it.vin.dev.menzione.logica;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.ViaggiUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

@SuppressWarnings("Duplicates")
public class PdfReportBuilderRemote {
    private static final String PATHFILE = "Exports\\";

    private Logger logger = LogManager.getLogger(this.getClass());
    private List<Viaggio> nordList;
    private List<Viaggio> sudList;
    private List<Viaggio> nordPinnedList;
    private List<Viaggio> sudPinnedList;
    private List<Ordine> ordiniSalitaList;
    private List<Ordine> ordiniDiscesaList;
    private List<Nota> noteList;
    private String fermi;
    private String nonAss;

    private File exportFile;
    private String dateStringCompact;
    private String dateStringExtended;

    private DatabaseService dbs;

    private ResourceBundle strings = ResourceBundle.getBundle("Localization/PdfReportStrings");

    public PdfReportBuilderRemote(Date date) throws Exception {
        logger.info("Started report builder construction");
        this.dateStringCompact = ViaggiUtils.createStringFromDate(date, true);
        this.dateStringExtended = ViaggiUtils.createStringFromDate(date, false);
        this.exportFile = new File(PATHFILE + dateStringCompact + ".pdf");

        this.nordList = new ArrayList<>();
        this.sudList = new ArrayList<>();
        this.nordPinnedList = new ArrayList<>();
        this.sudPinnedList = new ArrayList<>();
        this.ordiniSalitaList = new ArrayList<>();
        this.ordiniDiscesaList = new ArrayList<>();
        this.noteList = new ArrayList<>();

        logger.info("Opening database connection...");
        this.dbs = DatabaseService.create();
//        this.dbs.openConnection();

        logger.info("Opening file...");
        boolean result = true;
        if (exportFile.exists()) {
            result = exportFile.delete();
        }

        if (result) {
            result = exportFile.createNewFile();
        }

        if (!result) {
            throw new IOException("Export file creation error");
        }

        logger.info("Retrieving viaggi from database...");
        Vector<Viaggio> nord = dbs.getViaggiBy(Viaggio.NORD, date);
        Vector<Viaggio> sud = dbs.getViaggiBy(Viaggio.SUD, date);

        for (Viaggio viaggio : nord) {
            if (viaggio.isPinned()) {
                this.nordPinnedList.add(viaggio);
            } else {
                this.nordList.add(viaggio);
            }
        }

        for (Viaggio viaggio : sud) {
            if (viaggio.isPinned()) {
                this.sudPinnedList.add(viaggio);
            } else {
                this.sudList.add(viaggio);
            }
        }

        logger.info("Retrieving ordini from database...");
        Vector<Ordine> ordini = dbs.getOrdiniByDate(date);
        for (Ordine o : ordini) {
            if (Ordine.SALITA.equalsIgnoreCase(o.getType())) {
                this.ordiniSalitaList.add(o);
            } else if (Ordine.DISCESA.equalsIgnoreCase(o.getType())) {
                this.ordiniDiscesaList.add(o);
            }
        }

        logger.info("Retrieving note from database...");
        Vector<Nota> note = dbs.getNoteByDate(date);
        for (Nota n : note) {
            if (Nota.NOTA.equalsIgnoreCase(n.getTipo())) {
                this.noteList.add(n);
            } else if (Nota.FERMI.equalsIgnoreCase(n.getTipo())) {
                this.fermi = n.getTesto();
            } else if (Nota.NONASS.equals(n.getTipo())) {
                this.nonAss = n.getTesto();
            }
        }

        logger.info("Closing database connection...");
        dbs.closeConnection();
        logger.info("Report builder creation finished!");
    }

    public File startExport() throws Exception {
        logger.info("Report export started");
        logger.info("Opening file...");
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(exportFile));
        writer.open();
        document.open();


        logger.info("Writing document metadata...");
        document.addTitle("GestioneViaggi Export " + dateStringCompact);
        document.addAuthor("GestioneViaggi");
        document.addCreator("GestioneViaggi");

        logger.info("Writing data...");
        createHeader(document);
        createTable(document);

        logger.info("Closing file...");
        document.close();
        writer.close();

        logger.info("Report export finished");
        return exportFile;
    }

    private void createHeader(Document document) throws DocumentException {
        Font font = new Font(FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.BLACK);
        Font font2 = new Font(FontFamily.HELVETICA, 13, Font.BOLD, GrayColor.BLACK);
        PdfPTable table = new PdfPTable(new float[]{10f, 2f});
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase(strings.getString("report.title"), font));
        PdfPCell cell2 = new PdfPCell(new Phrase(dateStringExtended, font2));

        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell2.setVerticalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell2.disableBorderSide(PdfPCell.LEFT);

        table.addCell(cell);
        table.addCell(cell2);

        document.add(table);
    }

    private void createTable(Document document) throws DocumentException, ExportException {
        boolean nordOK;
        boolean sudOk;
        boolean ordiniSalitaOK;
        boolean ordiniDiscesaOK;


        PdfPTable nordPdfTable = creaViaggiTable(this.nordList, Consts.VIAGGI_TM_TYPE_NORD);
        PdfPTable sudPdfTable = creaViaggiTable(this.sudList, Consts.VIAGGI_TM_TYPE_SUD);
        PdfPTable nordPinPdfTable = creaViaggiTable(this.nordPinnedList, Consts.VIAGGI_TM_TYPE_NORD);
        PdfPTable sudPinPdfTable = creaViaggiTable(this.sudPinnedList, Consts.VIAGGI_TM_TYPE_SUD);

        nordOK = nordPdfTable != null && nordPinPdfTable != null;

        sudOk = sudPdfTable != null && sudPinPdfTable != null;

        PdfPTable ordiniSalitaPdfTable = creaOridniTable(ordiniSalitaList);
        ordiniSalitaOK = ordiniSalitaPdfTable != null;


        PdfPTable ordiniDiscesaPdfTable = creaOridniTable(ordiniDiscesaList);
        ordiniDiscesaOK = ordiniDiscesaPdfTable != null;

        if (nordOK && sudOk && ordiniDiscesaOK && ordiniSalitaOK) {
            document.add(new Paragraph(strings.getString("report.paragraph.nord")));
            addEmptyLine(document, 1);
            document.add(nordPdfTable);
            document.add(nordPinPdfTable);
            addEmptyLine(document, 2);
            document.add(new Paragraph(strings.getString("report.paragraph.sud")));
            addEmptyLine(document, 1);
            document.add(sudPdfTable);
            document.add(sudPinPdfTable);
            addEmptyLine(document, 1);
            document.add(new LineSeparator());
            document.add(new Paragraph(strings.getString("report.paragraph.ordini.salita")));
            addEmptyLine(document, 1);
            document.add(ordiniSalitaPdfTable);
            addEmptyLine(document, 2);
            document.add(new Paragraph(strings.getString("report.paragraph.ordini.discesa")));
            addEmptyLine(document, 1);
            document.add(ordiniDiscesaPdfTable);
            addEmptyLine(document, 1);
            document.add(new LineSeparator());
            document.add(new Paragraph(strings.getString("report.paragraph.fermi")));
            document.add(new Phrase(fermi));
            addEmptyLine(document, 2);
            document.add(new Paragraph(strings.getString("report.paragraph.non.assicurati")));
            document.add(new Phrase(nonAss));
            addEmptyLine(document, 2);
            document.add(new Phrase(strings.getString("report.paragraph.note") + "\n"));

            for (Nota n : this.noteList) {
                document.add(new Phrase(n.getTesto() + "\n"));
            }
        } else {
            throw new ExportException("Errore nell'esportazione. Riprovare.");
        }
    }

    private PdfPTable creaViaggiTable(List<Viaggio> viaggi, int viaggiTmType) {
        if (viaggi != null) {
            PdfPTable pdfTable = null;

            if (viaggiTmType == Consts.VIAGGI_TM_TYPE_NORD) {
                float[] larghezzaColonne = {1.7f, 3f, 3f, 5f, 1f};
                pdfTable = new PdfPTable(larghezzaColonne);
            } else if (viaggiTmType == Consts.VIAGGI_TM_TYPE_SUD) {
                float[] larghezzaColonne = {1.7f, 3f, 3f, 5f, 1f, 1f};
                pdfTable = new PdfPTable(larghezzaColonne);
            }

            pdfTable.setWidthPercentage(100);

            Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

            PdfPCell targaCell = new PdfPCell(new Phrase(strings.getString("report.viaggio.targa"), f));
            targaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            targaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(targaCell);
            PdfPCell caratteristicheCell = new PdfPCell(new Phrase(strings.getString("report.viaggio.caratteristiche"), f));
            caratteristicheCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            caratteristicheCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(caratteristicheCell);
            PdfPCell autistaCell = new PdfPCell(new Phrase(strings.getString("report.viaggio.autista"), f));
            autistaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            autistaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(autistaCell);
            PdfPCell noteCell = new PdfPCell(new Phrase(strings.getString("report.phrase.note"), f));
            noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(noteCell);

            if (viaggiTmType == Consts.VIAGGI_TM_TYPE_SUD) {
                PdfPCell litriCell = new PdfPCell(new Phrase(strings.getString("report.viaggio.litri"), f));
                litriCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                litriCell.setBackgroundColor(GrayColor.DARK_GRAY);
                pdfTable.addCell(litriCell);
            }

            PdfPCell selezionatoCell = new PdfPCell(new Phrase(strings.getString("report.phrase.sel"), f));
            selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(selezionatoCell);

            pdfTable.setHeaderRows(1);
            int i = 0;
            for (Viaggio v : viaggi) {
                PdfPCell c1 = new PdfPCell(new Phrase(v.getCamion().getTarga()));
                PdfPCell c2 = new PdfPCell(new Phrase(v.getCamion().getCaratteristiche()));
                PdfPCell c3 = new PdfPCell(new Phrase(v.getAutista()));
                PdfPCell c4 = new PdfPCell(new Phrase((v.getNote())));
                PdfPCell c5;

                PdfPCell litri = null;

                if (viaggiTmType == Consts.VIAGGI_TM_TYPE_SUD) {
                    litri = new PdfPCell(new Phrase("" + (v.getLitriB())));
                }


                if (v.isSelezionato()) {
                    c5 = new PdfPCell(new Phrase(strings.getString("report.phrase.yes")));
                } else {
                    c5 = new PdfPCell(new Phrase(strings.getString("report.phrase.no")));
                }


                i++;

                pdfTable.addCell(c1);
                pdfTable.addCell(c2);
                pdfTable.addCell(c3);
                pdfTable.addCell(c4);

                if (viaggiTmType == Consts.VIAGGI_TM_TYPE_SUD) {
                    pdfTable.addCell(litri);
                }

                pdfTable.addCell(c5);
            }

            if (i == viaggi.size()) {
                return pdfTable;
            }
        }
        return null;
    }

    private PdfPTable creaOridniTable(List<Ordine> ordini) {
        float[] ordiniColumnsWidth = {1f,3f,6f,1f};
        PdfPTable ordiniPdfTable = new PdfPTable(ordiniColumnsWidth);
        ordiniPdfTable.setWidthPercentage(100);

        if(ordini != null ) {

            Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

            PdfPCell dataCell = new PdfPCell(new Phrase(strings.getString("report.ordine.date"), f));
            dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(dataCell);
            PdfPCell clienteCell = new PdfPCell(new Phrase(strings.getString("report.ordine.cliente"), f));
            clienteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            clienteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(clienteCell);
            PdfPCell noteCell = new PdfPCell(new Phrase(strings.getString("report.phrase.note"), f));
            noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(noteCell);
            PdfPCell selezionatoCell = new PdfPCell(new Phrase(strings.getString("report.phrase.sel"), f));
            selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(selezionatoCell);

            ordiniPdfTable.setHeaderRows(1);
            int i = 0;

            for(Ordine o : ordini) {
                PdfPCell c1 = new PdfPCell(new Phrase(o.getData()));
                PdfPCell c2 = new PdfPCell(new Phrase(o.getCliente()));
                PdfPCell c3 = new PdfPCell(new Phrase(o.getNote()));
                PdfPCell c4;

                if(o.getSelezionato()) {
                    c4 = new PdfPCell(new Phrase(strings.getString("report.phrase.yes")));
                } else {
                    c4 = new PdfPCell(new Phrase(strings.getString("report.phrase.no")));
                }

                i++;

                ordiniPdfTable.addCell(c1);
                ordiniPdfTable.addCell(c2);
                ordiniPdfTable.addCell(c3);
                ordiniPdfTable.addCell(c4);
            }

            if(i == ordini.size()) {
                return ordiniPdfTable;
            }
        }
        return null;
    }

    private void addEmptyLine(Document doc, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            doc.add(new Paragraph(" "));
        }
    }
}
