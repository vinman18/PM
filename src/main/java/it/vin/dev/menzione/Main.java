package it.vin.dev.menzione;

import it.vin.dev.menzione.database_helper.DatabaseClient;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.logica.ConfigurationManager;
import it.vin.dev.menzione.logica.DatabaseService;
import it.vin.dev.menzione.main_frame.MainFrame;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;

public class Main {

    static {
        //http://logging.apache.org/log4j/2.x/manual/layouts.html#enable-jansi
        //https://stackoverflow.com/a/38860875
        System.setProperty("log4j2.skipJansi", "false");
    }

    private static VerboseLogger logger = VerboseLogger.create(Main.class);
    public Main() {

    }

    public static void main(String[] args) {
        logger.verbose("Avvio applicazione...");
        logger.info(new GregorianCalendar().getTimeZone().toString());

        System.out.println("--Ricorda di aggiornare dbversion (sia sull'aplicazione sia sul db)"
                + "se fai modifiche al database!!--");

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, exception) -> {logger.fatal(exception.getMessage(), exception); System.exit(1);}
        );

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.

            try {
                UIManager.setLookAndFeel(new NimbusLookAndFeel());
            } catch (UnsupportedLookAndFeelException e1) {
                e1.printStackTrace();
            }
        }

        try {
            logger.info("Loading config file...");
            ViaggiUtils.checkAndCreateAppFiles();
        } catch (IOException e) {
            logger.fatal(e);
        }

        logger.info("Loading configuration...");
        ConfigurationManager conf = ConfigurationManager.getInstance();

        logger.debug(conf.getDatabaseName());
        logger.debug(conf.getDatabaseLocation());
        logger.debug(conf.getDatabasePort());
        logger.debug(conf.getDatabaseUser());
        logger.debug(conf.getDatabasePassword());

        if (!conf.getConfiguration().containsKey(ConfigurationManager.USER)) {
            conf.getConfiguration().setProperty(ConfigurationManager.USER, System.getProperty("user.name"));
        }
/*

        String serverPolicyPath = "./java.policy";
        File policyFile = new File(serverPolicyPath);

        if(policyFile.exists()) {
            logger.info("Loading policy file...");
            System.setProperty("java.security.policy", serverPolicyPath);
            Policy.getPolicy().refresh();
            System.setSecurityManager(new SecurityManager());
            logger.info("Policy file loaded");
        } else {
            logger.warn("No policy file found on " + policyFile.getAbsolutePath());
            Msg.warn(null, "File di policy non trovato");
        }
*/

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    logger.info("Loading MainFrame...");
                    MainFrame frame = new MainFrame();

                    logger.info("Connection with remote DatabaseHelper server on ip " + conf.getDBHelperHost());
                    String id = UUID.randomUUID().toString();
                    DatabaseClient client = new DatabaseClient(id, conf.getLocalUser());
                    DatabaseHelperChannel helper = DatabaseHelperChannel.getInstance();
                    helper.connect(client);
                    logger.info("Remote DatabaseHelper connected");

                    frame.pack();
                    frame.setVisible(true);
                    logger.info("MainFrame loaded");
                } catch (Exception e) {
                    logger.fatal(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });

    }

}
