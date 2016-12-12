/*
 * Copyright 2016 Fritz Elfert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    
    @Option(name = "-h", aliases = {"--help"}, usage = "Print this help")
    private boolean help;

    @Option(name = "-V", aliases = {"--version"}, usage = "Print version info and exit")
    private boolean version;

    @Option(name = "-d", aliases = {"--debug"}, usage = "Enable debugging")
    private boolean debug;

    @Option(name = "-k", aliases = {"--keyfile"}, usage = "Specify key file")
    private File keyfile;

    @Option(name = "-a", aliases = {"--account"}, usage = "Specify account name")
    private String account;

    @Option(name = "-c", aliases = {"--crypto"}, usage = "Specify crypto version if not deductable by file extension")
    private WhatsAppCryptoVersion wcversion;

    @Argument
    private List<String> arguments = new ArrayList<>();

    private enum Action {
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

    private int doit(String[] args) throws IOException {
        final ParserProperties pp = ParserProperties.defaults()
            .withShowDefaults(false);
        final CmdLineParser parser = new CmdLineParser(this, pp);

        try {
            parser.parseArgument(args);
            if (debug) {
                final ch.qos.logback.classic.Logger root =
                    (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                root.setLevel(ch.qos.logback.classic.Level.DEBUG);
            }
            LOGGER.debug("debug enabled");
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        if (version) {
            System.out.println(
                    String.format("watools version %s, Copyright 2016 Fritz Elfert", Version.VERSION));
            System.out.println("Distributed under Apache Software License, Version 2.0");
            System.out.println("Visit https://github.com/felfert/watools");
            System.out.println();
            return 0;
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
                if (null == wcversion) {
                    try {
                        wcversion = WhatsAppCryptoVersion.fromFile(dbfile);
                    } catch (IllegalArgumentException x) {
                        System.err.println("Mandatory crypto version option is missing");
                        return 1;
                    }
                }
                WhatsAppCryptoInputStream wcs = null;
                if (wcversion.equals(WhatsAppCryptoVersion.CRYPT5)) {
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
                    wcs = new WhatsAppCryptoInputStream(dbfile, wcversion, keyfile);
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
