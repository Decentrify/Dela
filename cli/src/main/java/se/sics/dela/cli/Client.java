/*
 * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2016 Royal Institute of Technology (KTH)
 *
 * Dozy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more delaDatasetDetails.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.dela.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import static se.sics.dela.cli.Transfer.Setup.datasetName;
import static se.sics.dela.cli.Transfer.Util.bootstrap;
import static se.sics.dela.cli.Transfer.Rest.delaContents;
import static se.sics.dela.cli.Transfer.Rest.delaDatasetCancel;
import static se.sics.dela.cli.Transfer.Rest.delaDatasetDetails;
import static se.sics.dela.cli.Transfer.Rest.delaDownload;
import static se.sics.dela.cli.Transfer.Printer.delaContentsPrinter;
import static se.sics.dela.cli.Transfer.Printer.delaDatasetPrinter;
import static se.sics.dela.cli.Transfer.Printer.delaDownloadDetailsPrinter;
import static se.sics.dela.cli.Transfer.Target.delaClient;
import static se.sics.dela.cli.Transfer.Util.checkDelaVersion;
import static se.sics.dela.cli.Tracker.Rest.trackerSearch;
import static se.sics.dela.cli.Tracker.Printer.itemsPrinter;
import se.sics.dela.cli.cmd.CancelCmd;
import se.sics.dela.cli.cmd.ContentsCmd;
import se.sics.dela.cli.cmd.DetailsCmd;
import se.sics.dela.cli.cmd.DownloadCmd;
import se.sics.dela.cli.cmd.SearchCmd;
import se.sics.dela.cli.cmd.ServiceCmd;
import se.sics.dela.cli.dto.transfer.AddressJSON;
import se.sics.dela.cli.dto.HopsContentsSummaryJSON;
import se.sics.dela.cli.dto.tracker.SearchServiceDTO;
import se.sics.dela.cli.dto.TorrentExtendedStatusJSON;
import se.sics.dela.cli.util.PrintHelper;
import se.sics.ktoolbox.httpsclient.WebClient;
import se.sics.ktoolbox.util.trysf.Try;
import static se.sics.dela.cli.Tracker.Rest.trackerDatasetDetails;
import se.sics.ktoolbox.util.trysf.TryHelper.Joiner;
import se.sics.dela.cli.util.ExHelper.ClientException;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc0;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryStart;

public class Client {

  private static final boolean DEBUG_LOG = true;
  private static final boolean DEBUG_MODE = false;

  private static Map<String, Object> cmds = new HashMap<>();

  private static String getDelaDir() throws URISyntaxException {
    String delaDir;
    if (DEBUG_MODE) {
      String jarPath = Client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      delaDir = jarPath;
      //target dir
      delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
      delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
      //resources dir
      delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator)) 
        + File.separator + "src"
        + File.separator + "main"
        + File.separator + "resources";
    } else {
      String jarPath = Client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      delaDir = jarPath;
      delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
      delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
    }
    return delaDir;
  }

  public static void main(String[] args) {
    PrintWriter out = new PrintWriter(System.out);
    WebClient.setBuilder(new WebClient.BasicBuilder());
    String sourceDir;
    try {
      sourceDir = getDelaDir();
    } catch (URISyntaxException ex) {
      out.write("problems with dela location - source dir not accessible");
      System.exit(1);
      return;
    }
    JCommander jc = registerCmds();

    try {
      jc.parse(args);
      int retVal = executeCmd(sourceDir, out, jc.getParsedAlias());
      out.flush();
      if (retVal != 0) {
        System.exit(retVal);
      }
    } catch (MissingCommandException ex) {
      out.write(ex.getMessage());
      StringBuilder sb = new StringBuilder();
      jc.usage(sb);
      out.write(sb.toString());
      out.flush();
      System.exit(-1);
    } catch (ParameterException ex) {
      out.write(ex.getMessage());
      StringBuilder sb = new StringBuilder("\n");
      jc.usage(jc.getParsedCommand(), sb);
      out.write(sb.toString());
      out.flush();
      System.exit(-1);
    }
  }

  private static JCommander registerCmds() {
    JCommander.Builder jcb = JCommander.newBuilder();

    ServiceCmd serviceCmd = new ServiceCmd();
    cmds.put(Cmds.SERVICE, serviceCmd);
    jcb.addCommand(Cmds.SERVICE, serviceCmd);

    SearchCmd searchCmd = new SearchCmd();
    cmds.put(Cmds.SEARCH, searchCmd);
    jcb.addCommand(Cmds.SEARCH, searchCmd);

    DownloadCmd downloadCmd = new DownloadCmd();
    cmds.put(Cmds.DOWNLOAD, downloadCmd);
    jcb.addCommand(Cmds.DOWNLOAD, downloadCmd);

    ContentsCmd contentsCmd = new ContentsCmd();
    cmds.put(Cmds.CONTENTS, contentsCmd);
    jcb.addCommand(Cmds.CONTENTS, contentsCmd);

    DetailsCmd detailsCmd = new DetailsCmd();
    cmds.put(Cmds.DETAILS, detailsCmd);
    jcb.addCommand(Cmds.DETAILS, detailsCmd);

    CancelCmd cancelCmd = new CancelCmd();
    cmds.put(Cmds.CANCEL, cancelCmd);
    jcb.addCommand(Cmds.CANCEL, cancelCmd);

    return jcb.build();
  }

  private static int executeCmd(String delaDir, PrintWriter out, String cmdName) throws ParameterException {
    Try<String> delaVersion = checkDelaVersion(delaDir, out);
    PrintHelper.print(out, DEBUG_LOG, Joiner.successMsg(delaVersion, "dela version: %s"));
    Try<String> delaClient = delaClient(delaDir);
    switch (cmdName) {
      case Cmds.SERVICE: {
        ServiceCmd cmd = (ServiceCmd) cmds.get(Cmds.SERVICE);
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        switch (cmd.value()) {
          case STOP: {
            Try<String> stop = delaVersion
              .flatMap(Transfer.Daemon.stop(out, isWindows, delaDir))
              .flatMap(sleep(2000));
            Try<String> status = Joiner.map(stop, Joiner.combine(delaVersion, delaClient))
              .flatMap(Transfer.Rest.contact())
              .transform(Transfer.Printer.statusOK(), Transfer.Printer.statusFail());
            int ret = PrintHelper.print(out, DEBUG_LOG, status);
            return ret;
          }
          case START: {
            Try<String> start = delaVersion
              .flatMap(Transfer.Daemon.start(out, isWindows, delaDir));
            int retries = 5;
            Try<String> status;
            long sleepTime = 2000;
            do {
              Try<String> sleep = tryStart().flatMap(sleep(sleepTime));
              status = Joiner.map(start, Joiner.combine(delaVersion, delaClient))
                .flatMap(Transfer.Rest.contact())
                .transform(Transfer.Printer.statusOK(), Transfer.Printer.statusFail());
              if (Transfer.Translate.notReady(status)) {
                out.print("...");
                out.flush();
                retries--;
              } else {
                out.println(" ");
                out.flush();
                break;
              }
            } while (retries > 0);
            int ret = PrintHelper.print(out, DEBUG_LOG, status);
            return ret;
          }
          case STATUS: {
            Try<String> status = Joiner.combine(delaVersion, delaClient)
              .flatMap(Transfer.Rest.contact())
              .transform(Transfer.Printer.statusOK(), Transfer.Printer.statusFail());
            int ret = PrintHelper.print(out, DEBUG_LOG, status);
            return ret;
          }
        }
      }
      case Cmds.SEARCH: {
        SearchCmd cmd = (SearchCmd) cmds.get(Cmds.SEARCH);
        Try<SearchServiceDTO.Item[]> items = trackerSearch(cmd.term);
        int ret = PrintHelper.print(out, DEBUG_LOG, items, itemsPrinter());
        return ret;
      }
      case Cmds.DOWNLOAD: {
        DownloadCmd cmd = (DownloadCmd) cmds.get(Cmds.DOWNLOAD);
        Try<AddressJSON> delaContact = Joiner.combine(delaVersion, delaClient)
          .flatMap(Transfer.Rest.contact());
        PrintHelper.print(out, DEBUG_LOG, Joiner.successMsg(delaContact, "dela client - running"));
        Try<String> downloadSetup = Joiner.map(delaContact, datasetName(delaDir, cmd));
        Try<List<AddressJSON>> bootstrap = Joiner.map(downloadSetup, Joiner.combine(delaVersion, delaClient))
          .flatMap(Transfer.Rest.contact())
          .flatMap(trackerDatasetDetails(cmd.datasetId))
          .map(bootstrap());
        Try<TorrentExtendedStatusJSON> delaDatasetDetails = Joiner.map(bootstrap, delaClient)
          .flatMap(delaDatasetDetails(cmd.datasetId));
        Try<String> delaDownload = Joiner.map(delaDatasetDetails, Joiner.combine(delaClient, downloadSetup, bootstrap))
          .flatMap(delaDownload(delaDir, cmd.datasetId));

        int ret = PrintHelper.print(out, DEBUG_LOG,
          Joiner.map(delaDownload, Joiner.combine(downloadSetup, delaDatasetDetails)),
          delaDownloadDetailsPrinter(Transfer.Setup.delaDownloadDir(delaDir), cmd.datasetId));
        return ret;
      }
      case Cmds.CONTENTS: {
        ContentsCmd cmd = (ContentsCmd) cmds.get(Cmds.CONTENTS);
        Try<AddressJSON> delaContact = Joiner.combine(delaVersion, delaClient)
          .flatMap(Transfer.Rest.contact());
        PrintHelper.print(out, DEBUG_LOG, Joiner.successMsg(delaContact, "dela client - running"));
        Try<HopsContentsSummaryJSON.Hops> delaContents = Joiner.map(delaContact, delaClient)
          .flatMap(delaContents());
        int ret = PrintHelper.print(out, DEBUG_LOG, delaContents, delaContentsPrinter());
        return ret;
      }
      case Cmds.DETAILS: {
        DetailsCmd cmd = (DetailsCmd) cmds.get(Cmds.DETAILS);
        Try<AddressJSON> delaContact = Joiner.combine(delaVersion, delaClient)
          .flatMap(Transfer.Rest.contact());
        PrintHelper.print(out, DEBUG_LOG, Joiner.successMsg(delaContact, "dela client - running"));
        Try<TorrentExtendedStatusJSON> datasetDetails = Joiner.map(delaContact, delaClient)
          .flatMap(delaDatasetDetails(cmd.datasetId));
        int ret = PrintHelper.print(out, DEBUG_LOG, datasetDetails, delaDatasetPrinter());
        return ret;
      }
      case Cmds.CANCEL: {
        CancelCmd cmd = (CancelCmd) cmds.get(Cmds.CANCEL);
        Try<AddressJSON> delaContact = Joiner.combine(delaVersion, delaClient)
          .flatMap(Transfer.Rest.contact());
        PrintHelper.print(out, DEBUG_LOG, Joiner.successMsg(delaContact, "dela client - running"));
        Try<String> cancelDataset = Joiner.map(delaContact, delaClient)
          .flatMap(delaDatasetCancel(cmd.datasetId));
        int ret = PrintHelper.print(out, DEBUG_LOG, cancelDataset);
        return ret;
      }
      default:
        return -1;
    }
  }

  public static class Cmds {

    public static final String SERVICE = "service";
    public static final String SEARCH = "search";
    public static final String DOWNLOAD = "download";
    public static final String CONTENTS = "contents";
    public static final String DETAILS = "details";
    public static final String CANCEL = "cancel";
  }

  public static <O> BiFunction<O, Throwable, Try<String>> sleep(long time) {
    return tryFSucc0(() -> {
      try {
        Thread.sleep(time);
        return new Try.Success("slept 2s");
      } catch (InterruptedException ex) {
        return new Try.Failure(new ClientException(ex));
      }
    });
  }
}
