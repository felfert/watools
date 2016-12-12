package com.github.felfert.watools;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decrypt a WhatsApp backup.
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    
    @Option(name="-h", aliases={"--help"}, usage="Print this help")
    private boolean help;

    @Option(name="-d", aliases={"--debug"}, usage="Enable debugging")
    private boolean debug;

    @Option(name="-k", aliases={"--keyfile"}, usage="Specify key file")
    private File keyfile;

    @Option(name="-a", aliases={"--account"}, usage="Specify account name")
    private String account;

    @Argument
    private List<String> arguments = new ArrayList<>();

    private static enum Action {
        DECRYPT;
    }

    private String getFirstArg() throws IndexOutOfBoundsException {
        final String ret = arguments.get(0);
        arguments.remove(0);
        return ret;
    }

    private Action getAction() {
        String cmd = null;
        try {
            cmd = getFirstArg();
            return Action.valueOf(cmd.toUpperCase());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Missing command");
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("No such command: " + cmd);
            return null;
        }
    }

    private String getArg(final String error) {
        try {
            return getFirstArg();
        } catch (IndexOutOfBoundsException e) {
            if (null != error) {
                System.err.println(error);
            }
            return null;
        }
    }

    /*private String getArg(final String error, final String alt) {
        if (null != alt) {
            return alt;
        }
        return getArg(error);
    }*/

    WhatsAppCryptoInputStream.CRYPTVERSION selectTypeFromExtension(final File f) {
        try {
            String name = f.getName();
            return WhatsAppCryptoInputStream.CRYPTVERSION.valueOf(
                    "WA" + name.substring(name.lastIndexOf(".") + 1).toUpperCase());
        } catch (Exception e) {
            System.err.println("DB file name must end with .crypt5 .crypt7 .crypt8 or .crypt12");
            return null;
        }
    }

    private int doit(String[] args) throws IOException {
        final ParserProperties pp = ParserProperties.defaults()
            .withShowDefaults(false);
        final CmdLineParser parser = new CmdLineParser(this, pp);

        try {
            parser.parseArgument(args);
            if (debug) {
                final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            LOGGER.debug("debug enabled");
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (help) {
            System.out.println("Usage: java -jar watools-<version>.jar command [options..]");
            System.out.println("Valid commands:");
            System.out.println(java.util.Arrays.asList(Action.values()) + "\n");
            System.out.println("Valid options:");
            parser.printUsage(System.out);
            System.out.println();
            final Action action = getAction();
            if (null != action) {
                switch (action) {
                    case DECRYPT:
                        System.out.println("decrypt [-k keyfile|-a account] dbfile outfile");
                        break;
                }
            }
            return 0;
        }

        final Action action = getAction();
        if (null == action) {
            return 1;
        }
        switch (action) {
            case DECRYPT:
                String dbfileName = getArg("Missing positional dbfile argument");
                if (null == dbfileName) {
                    return 1;
                }
                File dbfile = new File(dbfileName);
                WhatsAppCryptoInputStream.CRYPTVERSION v = selectTypeFromExtension(dbfile);
                if (null == v) {
                    return 1;
                }
                WhatsAppCryptoInputStream wcs = null;
                if (v.equals(WhatsAppCryptoInputStream.CRYPTVERSION.WACRYPT5)) {
                    if (null == account) {
                        System.err.println("Required account parameter is missing");
                        return 1;
                    }
                    wcs = new WhatsAppCryptoInputStream(dbfile, account);
                } else {
                    if (null == keyfile) {
                        System.err.println("Required key file parameter is missing");
                        return 1;
                    }
                    wcs = new WhatsAppCryptoInputStream(dbfile, v, keyfile);
                }
                LOGGER.debug("{}", wcs);
                String outfileName = getArg("Missing positional outfile argument");
                if (null == outfileName) {
                    return 1;
                }
                File outfile = new File(outfileName);
                Files.copy(wcs, outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                break;
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {
        System.exit(new App().doit(args));
    }
}
