/*
--------------------------------------------------------------------------------
SPADE - Support for Provenance Auditing in Distributed Environments.
Copyright (C) 2011 SRI International

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--------------------------------------------------------------------------------
 */
package spade.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import jline.ArgumentCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.MultiCompletor;
import jline.NullCompletor;
import jline.SimpleCompletor;
import spade.core.Kernel;

public class QueryClient {

    private static PrintStream outputStream;
    private static PrintStream errorStream;
    private static PrintStream SPADEQueryIn;
    private static BufferedReader SPADEQueryOut;
    // private static String inputPath;
    private static String nullString;
    private static volatile boolean shutdown;
    private static final String historyFile = "../cfg/query.history";
    private static final String COMMAND_PROMPT = "-> ";
    private static final int THREAD_SLEEP_DELAY = 10;

    public static void main(String args[]) {

        outputStream = System.out;
        errorStream = System.err;
        // inputPath = args[0];
        // outputPath = args[1];
        shutdown = false;
        nullString = "null";

        /*
        try {
            // Create the output pipe for queries.
            int exitValue = Runtime.getRuntime().exec("mkfifo " + outputPath).waitFor();
            if (exitValue != 0) {
                throw new Exception();
            }
            SPADEQueryIn = new PrintStream(new FileOutputStream(inputPath));
        } catch (Exception exception) {
            errorStream.println("Query pipes not ready!");
            System.exit(0);
        }
         * 
         */

        Runnable outputReader = new Runnable() {

            public void run() {
                try {
                    SocketAddress sockaddr = new InetSocketAddress("localhost", Kernel.LOCAL_QUERY_PORT);
                    Socket remoteSocket = new Socket();
                    remoteSocket.connect(sockaddr, Kernel.TIMEOUT);
                    OutputStream outStream = remoteSocket.getOutputStream();
                    InputStream inStream = remoteSocket.getInputStream();
                    SPADEQueryOut = new BufferedReader(new InputStreamReader(inStream));
                    SPADEQueryIn = new PrintStream(outStream);

                    // SPADEQueryOut = new BufferedReader(new FileReader(outputPath));
                    while (!shutdown) {
                        if (SPADEQueryOut.ready()) {
                            // This thread keeps reading from the output pipe and
                            // printing to the current output stream.
                            String outputLine = SPADEQueryOut.readLine();
                            if (outputLine != null) {
                                outputStream.println(outputLine);
                            }
                            if (outputLine.equals("")) {
                                outputStream.print(COMMAND_PROMPT);
                            }
                        }
                        Thread.sleep(THREAD_SLEEP_DELAY);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace(errorStream);
                }
            }
        };
        new Thread(outputReader).start();

        try {

            outputStream.println("");
            outputStream.println("SPADE 2.0 Query Client");
            outputStream.println("");

            // Set up command history and tab completion.

            ConsoleReader commandReader = new ConsoleReader();
            commandReader.getHistory().setHistoryFile(new File(historyFile));

            List<Completor> argCompletor = new LinkedList<Completor>();
            argCompletor.add(new SimpleCompletor(new String[]{"query"}));
            argCompletor.add(new NullCompletor());

            List<Completor> completors = new LinkedList<Completor>();
            completors.add(new ArgumentCompletor(argCompletor));

            commandReader.addCompletor(new MultiCompletor(completors));

            SPADEQueryIn.println(nullString + " ");
            while (true) {
                try {
                    String line = commandReader.readLine();
                    if (line.equalsIgnoreCase("exit")) {
                        // On shutdown, remove the output pipe created earlier.
                        shutdown = true;
                        Runtime.getRuntime().exec("rm -f " + nullString).waitFor();
                        break;
                    } else {
                        // The output path is embedded in each query sent to SPADE
                        // as the first token of the query. This is to allow multiple
                        // query clients to work simultaneously with SPADE.
                        SPADEQueryIn.println(nullString + " " + line);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
