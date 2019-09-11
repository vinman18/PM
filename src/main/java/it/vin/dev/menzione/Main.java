package it.vin.dev.menzione;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Policy;
import java.util.Random;
import java.util.UUID;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import it.vin.dev.menzione.database_helper.DatabaseClient;
import it.vin.dev.menzione.database_helper.DatabaseHelperChannel;
import it.vin.dev.menzione.logica.Configuration;
import it.vin.dev.menzione.main_frame.MainFrame;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.IOException;
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
            ViaggiUtils.checkAndCreateConfigFile();
        } catch (IOException e) {
            logger.fatal(e);
        }

        logger.info("Loading configuration...");
        Configuration conf = Configuration.getInstance();

        logger.debug(conf.getDbName());
        logger.debug(conf.getLocation());
        logger.debug(conf.getDbPort());
        logger.debug(conf.getDbUser());
        logger.debug(conf.getDbPassword());
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

                    DatabaseClient client = null;
//                    try {
                        logger.info("Connection with remote DatabaseHelper server on ip " + conf.getDbhelperHost());
                        String id = UUID.randomUUID().toString();
                        client = new DatabaseClient(id, conf.getUser());
                        DatabaseHelperChannel helper = DatabaseHelperChannel.getInstance();
                        helper.connect(client);
                        logger.info("Remote DatabaseHelper connected");
                   /* } catch (NotBoundException | RemoteException e) {
                        logger.debug("Connection with remote DatabaseHelper failed. " + e.getMessage(), e);
                        logger.warn("Connection with remote DatabaseHelper failed. " + e.getMessage());
                        e.printStackTrace();
                        frame.getMessageField().setWarnMessage("Impossibile connettersi al servizio di notifica. " +
                                "Le modifiche effettuate non verranno notificate agli altri clients.");
                    }*/

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
