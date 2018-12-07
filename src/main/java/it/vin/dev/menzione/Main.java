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
import it.vin.dev.menzione.database_helper.IDatabaseHelper;
import it.vin.dev.menzione.main_frame.MainFrame;
import it.vin.dev.menzione.logica.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static Logger logger = LogManager.getLogger(Main.class);
    public Main() {

    }

    public static void main(String[] args) {
        logger.info("Avvio applicazione...");

        System.out.println("--Ricorda di aggiornare dbversion (sia sull'aplicazione sia sul db)"
                + "se fai modifiche al database!!--");

        Thread.setDefaultUncaughtExceptionHandler(
                (thread, exception) -> logger.fatal(exception.getMessage(), exception)
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
            ViaggiUtils.checkAndCreateConfigFile();
        } catch (IOException e) {
            logger.fatal(e);
        }

        Configuration conf = Configuration.getInstance();

        logger.debug(conf.getDbName());
        logger.debug(conf.getLocation());
        logger.debug(conf.getDbPort());
        logger.debug(conf.getUser());
        logger.debug(conf.getPassword());

        String serverPolicyPath = "./java.policy";
        File policyFile = new File(serverPolicyPath);

        if(policyFile.exists()) {
            System.setProperty("java.security.policy", serverPolicyPath);
            Policy.getPolicy().refresh();

            System.setSecurityManager(new SecurityManager());
        } else {
            logger.warn("No policy file found on " + policyFile.getAbsolutePath());
            Msg.warn(null, "File di policy non trovato");
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();

                    DatabaseClient client = null;
                    try {
                        String id = UUID.randomUUID().toString();
                        client = new DatabaseClient(id, System.getProperty("user.name"));
                        Registry registry = LocateRegistry.getRegistry(conf.getDbhelperHost(), conf.getDbhelperPort());
                        IDatabaseHelper helper = (IDatabaseHelper) registry.lookup("DatabaseHelper");
                        helper.connect(client, client.getId());
                        DatabaseHelperChannel.getInstance().setHelper(helper);
                        DatabaseHelperChannel.getInstance().setClient(client);
                    } catch (NotBoundException | RemoteException e) {
                        logger.debug("Connection with remote DatabaseHelper failed. " + e.getMessage(), e);
                        logger.warn("Connection with remote DatabaseHelper failed. " + e.getMessage());
                        e.printStackTrace();
                        frame.getMessageField().setWarnMessage("Impossibile connettersi al servizio di notifica. " +
                                "Le modifiche effettuate non verranno notificate agli altri clients.");
                    }

                    if(client != null) {
                        client.setListener(frame);
                    }

                    frame.pack();
                    frame.setVisible(true);
                } catch (Exception e) {
                    logger.fatal(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });

    }

}
