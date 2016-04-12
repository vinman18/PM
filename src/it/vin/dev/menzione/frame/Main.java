package it.vin.dev.menzione.frame;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.logica.PdfReportBuilder;

public class Main {

	private static Logger logger;
	public Main() {
		// TODO Auto-generated constructor stub

	}

	public static void main(String[] args) {
		
		System.out.println("COMMITTTTTT");
		System.out.println("Secondo commit!!!!!");
		System.err.println("Terzo ed ultimo commit");
		
		logger = Logger.getGlobal();
		System.out.println("--Ricorda di aggiornare dbversion (sia sull'aplicazione sia sul db)\n"
				+ "se fai modifiche al database!!--");
		try {
			FileHandler fh = new FileHandler("Prova.log");
			logger.addHandler(fh);

		} catch (SecurityException | IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			boolean trovato = false;
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Windows".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					trovato = true;
					break;
				}
			}

			if(trovato){

				logger.info("Stile trovato");
			}else{
				logger.info("Stile NON trovato");
			}

		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look and feel.

			logger.log(Level.SEVERE, e.getMessage(), e);
			try {
				UIManager.setLookAndFeel(new NimbusLookAndFeel());
			} catch (UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("config.txt"));
			String sCurrentLine;

			int i = -1;
			while ((sCurrentLine = br.readLine()) != null) {
				i++;
				switch(i){
				case 0: Configuration.setIp(sCurrentLine); break;
				case 1: Configuration.setUser(sCurrentLine); break;
				case 2: Configuration.setPassword(sCurrentLine); break;
				default: {}
				}
			}

			System.out.println(Configuration.getIp());
			System.out.println(Configuration.getUser());
			System.out.println(Configuration.getPassword());

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Calendar cal = Calendar.getInstance();
			Configuration.setLogfile(dateFormat.format(cal.getTime()));
			File f = new File(Configuration.getLogfile()+"");
			// Works for both Windows and Linux
			f.getParentFile().mkdirs(); 
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
