package it.vin.dev.menzione.logica;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.server.ExportException;
import java.util.Vector;

import javax.swing.JTable;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;

import com.itextpdf.text.pdf.draw.LineSeparator;
import it.vin.dev.menzione.Consts;
import it.vin.dev.menzione.main_frame.NoteTableModel;
import it.vin.dev.menzione.main_frame.OrdiniTable;
import it.vin.dev.menzione.main_frame.OrdiniTableModel;
import it.vin.dev.menzione.frame.ViaggiJTable;
import it.vin.dev.menzione.frame.ViaggiTableModel;

@SuppressWarnings("Duplicates")
public class PdfReportBuilder {

    private ViaggiJTable nord;
    private ViaggiJTable sud;
    private ViaggiJTable nordPinned;
    private ViaggiJTable sudPinned;
    private OrdiniTable ordiniSalitaTable;
    private OrdiniTable ordiniDiscesaTable;
    private JTable noteTable;
    private String fermi;
    private String nonAss;
    private String data;
    private static final String PATHFILE = "Exports\\";

    public PdfReportBuilder(ViaggiJTable nord, ViaggiJTable sud, ViaggiJTable nordPinned, ViaggiJTable sudPinned, OrdiniTable ordiniSalitaTable, OrdiniTable ordiniDiscesaTable, JTable noteTable, String fermi, String nonAss, String data) throws Exception {
        this.nord = nord;
        this.sud = sud;
        this.nordPinned = nordPinned;
        this.sudPinned = sudPinned;
        this.ordiniSalitaTable = ordiniSalitaTable;
        this.ordiniDiscesaTable = ordiniDiscesaTable;
        this.noteTable = noteTable;
        this.data = data;
        this.fermi = fermi;
        this.nonAss = nonAss;
        File f = new File(PATHFILE+data+".pdf");
        f.createNewFile();
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(f));

        document.open();
        document.addTitle("Export" + data);
        document.addAuthor("GestioneViaggi");
        document.addCreator("GestioneViaggi");

        createHeader(document);
        createTable(document);
        document.close();


    }

    private void createHeader(Document document) throws DocumentException {
        Font font = new Font(FontFamily.HELVETICA, 13, Font.NORMAL, GrayColor.BLACK);
        Font font2 = new Font(FontFamily.HELVETICA, 13, Font.BOLD, GrayColor.BLACK);
        PdfPTable table = new PdfPTable(new float[]{10f, 2f});
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase("GestioneViaggi - REPORT LOCAL", font));
        PdfPCell cell2 = new PdfPCell(new Phrase(data, font2));

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
        boolean nordOK = false;
        boolean sudOk = false;
        boolean ordiniSalitaOK = false;
        boolean ordiniDiscesaOK = false;


        PdfPTable nordPdfTable = creaViaggiTable(this.nord);
        PdfPTable sudPdfTable = creaViaggiTable(this.sud);
        PdfPTable nordPinPdfTable = creaViaggiTable(this.nordPinned);
        PdfPTable sudPinPdfTable = creaViaggiTable(this.sudPinned);

        if(nordPdfTable != null && nordPinPdfTable != null) nordOK =true;
        else nordOK = false;

        if(sudPdfTable != null && sudPinPdfTable != null) sudOk = true;
        else sudOk = false;

        PdfPTable ordiniSalitaPdfTable = creaOridniTable(ordiniSalitaTable);
        if(ordiniSalitaPdfTable != null) ordiniSalitaOK =true;
        else ordiniSalitaOK = false;


        PdfPTable ordiniDiscesaPdfTable = creaOridniTable(ordiniDiscesaTable);
        if(ordiniDiscesaPdfTable != null) ordiniDiscesaOK =true;
        else ordiniDiscesaOK = false;

        Vector<Nota> note = ((NoteTableModel) noteTable.getModel()).getData();

        if(nordOK && sudOk && ordiniDiscesaOK && ordiniSalitaOK){
            document.add(new Paragraph("NORD\n"));
            addEmptyLine(document, 1);
            document.add(nordPdfTable);
            document.add(nordPinPdfTable);
            addEmptyLine(document, 2);
            document.add(new Paragraph("SUD\n"));
            addEmptyLine(document, 1);
            document.add(sudPdfTable);
            document.add(sudPinPdfTable);
            addEmptyLine(document, 1);
            document.add(new LineSeparator());
            document.add(new Paragraph("ORDINI SALITA\n"));
            addEmptyLine(document, 1);
            document.add(ordiniSalitaPdfTable);
            addEmptyLine(document, 2);
            document.add(new Paragraph("ORDINI DISCESA\n"));
            addEmptyLine(document, 1);
            document.add(ordiniDiscesaPdfTable);
            addEmptyLine(document, 1);
            document.add(new LineSeparator());
            document.add(new Paragraph("FERMI\n"));
            document.add(new Phrase(fermi));
            addEmptyLine(document, 2);
            document.add(new Paragraph("NON ASSICURATI\n"));
            document.add(new Phrase(nonAss));
            addEmptyLine(document, 2);
            document.add(new Phrase("NOTE\n"));

            for(Nota n : note){
                document.add(new Phrase(n.getTesto()+"\n"));
            }
        }else{
            throw new ExportException("Errore nell'esportazione. Riprovare.");
        }
    }

    private PdfPTable creaViaggiTable(ViaggiJTable viaggiTable){
        if(viaggiTable != null ){
            ViaggiTableModel tm = (ViaggiTableModel) viaggiTable.getModel();
            Vector<Viaggio> viaggi = tm.getData();

            PdfPTable pdfTable=null;
            if(tm.getType() == Consts.VIAGGI_TM_TYPE_NORD){
                float[] larghezzaColonne = {1.5f,3f,3f,5f,1f};
                pdfTable = new PdfPTable(larghezzaColonne);
            }else if(tm.getType() == Consts.VIAGGI_TM_TYPE_SUD){
                float[] larghezzaColonne = {1.5f,3f,3f,5f,1f,1f};
                pdfTable = new PdfPTable(larghezzaColonne);
            }


            pdfTable.setWidthPercentage(100);

            Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

            PdfPCell targaCell = new PdfPCell(new Phrase("Targa",f));
            targaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            targaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(targaCell);
            PdfPCell caratteristicheCell = new PdfPCell(new Phrase("Caratteristiche",f));
            caratteristicheCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            caratteristicheCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(caratteristicheCell);
            PdfPCell autistaCell = new PdfPCell(new Phrase("Autista",f));
            autistaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            autistaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(autistaCell);
            PdfPCell noteCell = new PdfPCell(new Phrase("Note",f));
            noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(noteCell);

            if(tm.getType() == Consts.VIAGGI_TM_TYPE_SUD){

                PdfPCell litriCell = new PdfPCell(new Phrase("Litri",f));
                litriCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                litriCell.setBackgroundColor(GrayColor.DARK_GRAY);
                pdfTable.addCell(litriCell);
            }

            PdfPCell selezionatoCell = new PdfPCell(new Phrase("Sel.",f));
            selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
            pdfTable.addCell(selezionatoCell);

            pdfTable.setHeaderRows(1);
            int i = 0;
            for(Viaggio v : viaggi){
                PdfPCell c1 = new PdfPCell(new Phrase(v.getCamion().getTarga()));
                PdfPCell c2 = new PdfPCell(new Phrase(v.getCamion().getCaratteristiche()));
                PdfPCell c3 = new PdfPCell(new Phrase(v.getAutista()));
                PdfPCell c4 = new PdfPCell(new Phrase((v.getNote())));
                PdfPCell c5;

                PdfPCell litri=null;

                if(tm.getType() == Consts.VIAGGI_TM_TYPE_SUD){
                    litri = new PdfPCell(new Phrase(""+(v.getLitriB())));
                }


                if(v.isSelezionato()){
                    c5 = new PdfPCell(new Phrase("Si"));
                }else{
                    c5 = new PdfPCell(new Phrase("No"));
                }


                i++;

                pdfTable.addCell(c1);
                pdfTable.addCell(c2);
                pdfTable.addCell(c3);
                pdfTable.addCell(c4);

                if(tm.getType() == Consts.VIAGGI_TM_TYPE_SUD){
                    pdfTable.addCell(litri);
                }

                pdfTable.addCell(c5);
            }

            if(i == viaggi.size()){
                return pdfTable;
            }
        }
        return null;
    }
    /*
    private PdfPTable creaSudTable(){
        PdfPTable sudPdfTable = new PdfPTable(larghezzaColonneSud);
        sudPdfTable.setWidthPercentage(100);

        if(sud != null ){
            ViaggiTableModel tm = (ViaggiTableModel) sud.getModel();
            sudViaggi = tm.getData();

            Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

            PdfPCell targaCell = new PdfPCell(new Phrase("Targa",f));
            targaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            targaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            sudPdfTable.addCell(targaCell);
            PdfPCell caratteristicheCell = new PdfPCell(new Phrase("Caratteristiche",f));
            caratteristicheCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            caratteristicheCell.setBackgroundColor(GrayColor.DARK_GRAY);
            sudPdfTable.addCell(caratteristicheCell);
            PdfPCell autistaCell = new PdfPCell(new Phrase("Autista",f));
            autistaCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            autistaCell.setBackgroundColor(GrayColor.DARK_GRAY);
            sudPdfTable.addCell(autistaCell);
            PdfPCell noteCell = new PdfPCell(new Phrase("Note",f));
            noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            sudPdfTable.addCell(noteCell);
            PdfPCell selezionatoCell = new PdfPCell(new Phrase("Sel.",f));
            selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
            sudPdfTable.addCell(selezionatoCell);

            sudPdfTable.setHeaderRows(1);
            int i = 0;

            for(Viaggio vv : sudViaggi){
                PdfPCell c1 = new PdfPCell(new Phrase(vv.getCamion().getTarga()));
                PdfPCell c2 = new PdfPCell(new Phrase(vv.getCamion().getCaratteristiche()));
                PdfPCell c3 = new PdfPCell(new Phrase(vv.getAutista()));
                PdfPCell c4 = new PdfPCell(new Phrase(vv.getNote()));
                PdfPCell c5 = new PdfPCell(new Phrase(""+vv.getLitriB()));;
                PdfPCell c6;

                if(vv.isSelezionato()){
                    c6 = new PdfPCell(new Phrase("Si"));
                }else{
                    c6 = new PdfPCell(new Phrase("No"));
                }

                if(i%2 != 0){
                    c1.setBackgroundColor(GrayColor.LIGHT_GRAY);
                    c2.setBackgroundColor(GrayColor.LIGHT_GRAY);
                    c3.setBackgroundColor(GrayColor.LIGHT_GRAY);
                    c4.setBackgroundColor(GrayColor.LIGHT_GRAY);
                    c5.setBackgroundColor(GrayColor.LIGHT_GRAY);
                    c6.setBackgroundColor(GrayColor.LIGHT_GRAY);
                }

                i++;

                sudPdfTable.addCell(c1);
                sudPdfTable.addCell(c2);
                sudPdfTable.addCell(c3);
                sudPdfTable.addCell(c4);
                sudPdfTable.addCell(c5);
                sudPdfTable.addCell(c6);
            }

            if(i == sudViaggi.size()){
                return sudPdfTable;
            }
        }

        return null;
    }
    */
	/*
	private PdfPTable creaOrdiniDiscesaTable(){
		float[] larghezzaColonneOrdiniDiscesa = {1f,3f,6f,1f};
		PdfPTable ordiniDiscesaPdfTable = new PdfPTable(larghezzaColonneOrdiniDiscesa);
		ordiniDiscesaPdfTable.setWidthPercentage(100);


		if(ordiniDiscesaTable != null){


			OrdiniTableModel tm = (OrdiniTableModel) ordiniDiscesaTable.getModel();
			ordiniDiscesa = tm.getData();

			Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

			PdfPCell dataCell = new PdfPCell(new Phrase("Data",f));
			dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			dataCell.setBackgroundColor(GrayColor.DARK_GRAY);
			ordiniDiscesaPdfTable.addCell(dataCell);
			PdfPCell clienteCell = new PdfPCell(new Phrase("Cliente",f));
			clienteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			clienteCell.setBackgroundColor(GrayColor.DARK_GRAY);
			ordiniDiscesaPdfTable.addCell(clienteCell);
			PdfPCell noteCell = new PdfPCell(new Phrase("Note",f));
			noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
			ordiniDiscesaPdfTable.addCell(noteCell);
			PdfPCell selezionatoCell = new PdfPCell(new Phrase("Sel.",f));
			selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
			ordiniDiscesaPdfTable.addCell(selezionatoCell);

			ordiniDiscesaPdfTable.setHeaderRows(1);
			int i = 0;

			for(Ordine oo : ordiniDiscesa){
				System.out.println(oo);
				PdfPCell c1 = new PdfPCell(new Phrase(oo.getData()));
				PdfPCell c2 = new PdfPCell(new Phrase(oo.getCliente()));
				PdfPCell c3 = new PdfPCell(new Phrase(oo.getNote()));
				PdfPCell c4;

				if(oo.getSelezionato()){
					c4 = new PdfPCell(new Phrase("Si"));
				}else{
					c4 = new PdfPCell(new Phrase("No"));
				}

				if(i%2 != 0){
					c1.setBackgroundColor(GrayColor.LIGHT_GRAY);
					c2.setBackgroundColor(GrayColor.LIGHT_GRAY);
					c3.setBackgroundColor(GrayColor.LIGHT_GRAY);
					c4.setBackgroundColor(GrayColor.LIGHT_GRAY);
				}

				i++;

				ordiniDiscesaPdfTable.addCell(c1);
				ordiniDiscesaPdfTable.addCell(c2);
				ordiniDiscesaPdfTable.addCell(c3);
				ordiniDiscesaPdfTable.addCell(c4);
			}

			if(i == ordiniDiscesa.size()){
				ordiniDiscesaOK = true;
			}
		}
	}
	*/
    private PdfPTable creaOridniTable(OrdiniTable ordiniTable){

        float[] larghezzaColonneOrdini = {1f,3f,6f,1f};
        PdfPTable ordiniPdfTable = new PdfPTable(larghezzaColonneOrdini);
        ordiniPdfTable.setWidthPercentage(100);

        if(ordiniTable != null ){

            OrdiniTableModel tm = (OrdiniTableModel) ordiniTable.getModel();
            Vector<Ordine> ordiniSalita = tm.getData();

            Font f = new Font(FontFamily.HELVETICA, 11, Font.NORMAL, GrayColor.GRAYWHITE);

            PdfPCell dataCell = new PdfPCell(new Phrase("Data",f));
            dataCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dataCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(dataCell);
            PdfPCell clienteCell = new PdfPCell(new Phrase("Cliente",f));
            clienteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            clienteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(clienteCell);
            PdfPCell noteCell = new PdfPCell(new Phrase("Note",f));
            noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            noteCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(noteCell);
            PdfPCell selezionatoCell = new PdfPCell(new Phrase("Sel.",f));
            selezionatoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            selezionatoCell.setBackgroundColor(GrayColor.DARK_GRAY);
            ordiniPdfTable.addCell(selezionatoCell);

            ordiniPdfTable.setHeaderRows(1);
            int i = 0;

            for(Ordine o : ordiniSalita){
                PdfPCell c1 = new PdfPCell(new Phrase(o.getData()));
                PdfPCell c2 = new PdfPCell(new Phrase(o.getCliente()));
                PdfPCell c3 = new PdfPCell(new Phrase(o.getNote()));
                PdfPCell c4;

                if(o.getSelezionato()){
                    c4 = new PdfPCell(new Phrase("Si"));
                }else{
                    c4 = new PdfPCell(new Phrase("No"));
                }

                i++;

                ordiniPdfTable.addCell(c1);
                ordiniPdfTable.addCell(c2);
                ordiniPdfTable.addCell(c3);
                ordiniPdfTable.addCell(c4);
            }

            if(i==ordiniSalita.size()){
                return ordiniPdfTable;
            }
        }
        return null;
    }

    private static void addEmptyLine(Document doc, int number) throws DocumentException {
        for (int i = 0; i < number; i++) {
            doc.add(new Paragraph(" "));
        }
    }
}
