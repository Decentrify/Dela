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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.sics.dela.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import se.sics.dela.cli.cmd.CancelCmd;
import se.sics.dela.cli.cmd.ContentsCmd;
import se.sics.dela.cli.cmd.DetailsCmd;
import se.sics.dela.cli.cmd.DownloadCmd;
import se.sics.dela.cli.cmd.SearchCmd;
import se.sics.dela.cli.cmd.ServiceCmd;
import se.sics.dela.cli.dto.AddressJSON;
import se.sics.dela.cli.dto.ClusterAddressDTO;
import se.sics.dela.cli.dto.HopsContentsSummaryJSON;
import se.sics.dela.cli.dto.SearchServiceDTO;
import se.sics.dela.cli.dto.TorrentExtendedStatusJSON;
import se.sics.dela.cli.util.ManagedClientException;
import se.sics.dela.cli.util.UnknownClientException;

public class Client {

  private static Map<String, Object> cmds = new HashMap<>();

  private static String getDelaDir() throws URISyntaxException {
    String jarPath = Client.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
//    String jarPath = "/Users/Alex/Documents/_Work/Code/decentrify/run/dela/lib/dela.jar";
    String delaDir = jarPath;
    delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
    delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
    return delaDir;
  }

  public static void main(String[] args) {
    PrintWriter out = new PrintWriter(System.out);
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

  private static boolean checkDelaVersion(String delaDir, PrintWriter out) 
    throws UnknownClientException, ManagedClientException {
    String trackerDelaVersion = Tracker.Ops.delaVersion();
    String delaVersion = Dela.version(delaDir);
    String[] v1 = trackerDelaVersion.split("\\.");
    String[] v2 = delaVersion.split("\\.");
    if(v1.length != v2.length || v1.length != 3) {
      out.printf("WARNING! Version structure mismatch. Local:%s - Tracker:%s \n", delaVersion, trackerDelaVersion);
      return false;
    }
    if(!v1[0].equals(v2[0]) || !v1[1].equals(v2[1])) {
      out.printf("WARNING! Version incompatible. Local:%s - Tracker:%s \n", delaVersion, trackerDelaVersion);
      return false;
    }
    if(!v1[2].equals(v2[2])) {
      out.printf("Patch version mismatch. Protocol still be compatible. Local:%s - Tracker:%s \n", 
        delaVersion, trackerDelaVersion);
      return true;
    }
    return true;
  }
  
  private static int executeCmd(String delaDir, PrintWriter out, String cmdName) throws ParameterException {
    switch (cmdName) {
      case Cmds.SERVICE: {
        ServiceCmd cmd = (ServiceCmd) cmds.get(Cmds.SERVICE);
        try {
          boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
          boolean delaVersion = checkDelaVersion(delaDir, out);
          switch (cmd.value()) {
            case START: {
              if(!delaVersion) {
                return -1;
              }
              startDaemon(isWindows, delaDir);
              return 0;
            }
            case STOP: {
              stopDaemon(isWindows, delaDir);
              return 0;
            }
            case STATUS: {
              Dela.Ops.contact(delaDir);
              out.write("service online\n");
              return 0;
            }
          }
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      case Cmds.SEARCH: {
        SearchCmd cmd = (SearchCmd) cmds.get(Cmds.SEARCH);
        try {
          SearchServiceDTO.Item[] items = Tracker.Ops.search(cmd.term);
          Tracker.Printers.searchResultConsolePrinter(out).accept(items);
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        }
      }
      case Cmds.DOWNLOAD: {
        DownloadCmd cmd = (DownloadCmd) cmds.get(Cmds.DOWNLOAD);
        try {
          if(!checkDelaVersion(delaDir, out)) {
            return -1;
          }
          Dela.Ops.contact(delaDir);
          SearchServiceDTO.ItemDetails trackerDetails = Tracker.Ops.datasetDetails(cmd.datasetId);
          TorrentExtendedStatusJSON delaDetails = Dela.Ops.details(delaDir, cmd.datasetId);
          if (delaDetails.getTorrentStatus().equals("NONE")) {
            String datasetName = getDatasetName(delaDir, out, cmd);
            out.printf("Library: %s \n", delaDownloadDir(delaDir));
            out.printf("Saving dataset with id: %s as: %s \n", cmd.datasetId, datasetName);
            Dela.Ops.download(delaDir, delaDownloadDir(delaDir), cmd.datasetId, datasetName,
              getBootstrap(trackerDetails.getBootstrap()));
          } else {
            out.printf("Dataset with id: %s already active \n", cmd.datasetId);
          }
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      case Cmds.CONTENTS: {
        try {
          checkDelaVersion(delaDir, out);
          Dela.Ops.contact(delaDir);
          ContentsCmd cmd = (ContentsCmd) cmds.get(Cmds.CONTENTS);
          HopsContentsSummaryJSON.Hops contents = Dela.Ops.contents(delaDir);
          Dela.Printers.contentsConsolePrinter(out).accept(contents);
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      case Cmds.DETAILS: {
        try {
          checkDelaVersion(delaDir, out);
          Dela.Ops.contact(delaDir);
          DetailsCmd cmd = (DetailsCmd) cmds.get(Cmds.DETAILS);
          TorrentExtendedStatusJSON torrent = Dela.Ops.details(delaDir, cmd.datasetId);
          Dela.Printers.detailsConsolePrinter(out).accept(torrent);
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      case Cmds.CANCEL: {
        try {
          checkDelaVersion(delaDir, out);
          Dela.Ops.contact(delaDir);
          CancelCmd cmd = (CancelCmd) cmds.get(Cmds.CANCEL);
          Dela.Ops.cancel(delaDir, cmd.datasetId);
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      default:
        return -1;
    }
  }

  private static void startDaemon(boolean isWindows, String delaDir)
    throws ManagedClientException, UnknownClientException {
    ProcessBuilder builder = new ProcessBuilder();
    if (isWindows) {
      throw new ManagedClientException("windows not supported yet");
    } else {
      builder.command("sh", "-c", "./bin/daemon_start");
    }
    builder.directory(new File(delaDir));

    try {
      Process process = builder.start();
      StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
      ExecutorService es = Executors.newSingleThreadExecutor();
      Future f = es.submit(outStreamGobbler);
      if (process.waitFor() != 0) {
        throw new ManagedClientException("daemon start did not finish successfully");
      }
      f.get();
      es.shutdown();
      System.out.flush();
    } catch (InterruptedException | ExecutionException | IOException ex) {
      throw new UnknownClientException(ex);
    }
  }

  private static void stopDaemon(boolean isWindows, String delaDir)
    throws ManagedClientException, UnknownClientException {
    ProcessBuilder builder = new ProcessBuilder();
    if (isWindows) {
      throw new ManagedClientException("windows not supported yet");
    } else {
      builder.command("sh", "-c", "./bin/daemon_stop");
    }
    builder.directory(new File(delaDir));

    try {
      Process process = builder.start();
      StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
      ExecutorService es = Executors.newSingleThreadExecutor();
      Future f = es.submit(outStreamGobbler);
      if (process.waitFor() != 0) {
        throw new ManagedClientException("daemon stop did not finish successfully");
      }
      f.get();
      es.shutdown();
      System.out.flush();
    } catch (InterruptedException | ExecutionException | IOException ex) {
      throw new UnknownClientException(ex);
    }
  }

  private static class StreamGobbler implements Runnable {

    private final InputStream inputStream;
    private final Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
  }

  private static String getDatasetName(String delaDir, PrintWriter out, DownloadCmd cmd) throws ManagedClientException {
    String datasetName;
    if (cmd.datasetName != null) {
      datasetName = cmd.datasetName;
      if (datasetExists(delaDir, datasetName)) {
        StringBuilder sb = new StringBuilder();
        sb.append("Folder for dataset: ").append(cmd.datasetName);
        sb.append("already exists in library");
        sb.append("\nDelete folder or rename dataset.");
        throw new ManagedClientException(sb.toString());
      }
    } else {
      datasetName = cmd.datasetId;
      Random rand = new Random();
      while (datasetExists(delaDir, datasetName)) {
        datasetName = cmd.datasetId + "_" + rand.nextInt(Integer.MAX_VALUE);
      }
    }
    return datasetName;
  }

  private static String delaDownloadDir(String delaDir) {
    return delaDir + File.separator + "download";
  }

  private static boolean datasetExists(String delaDir, String datasetName) {
    File dataset = new File(delaDownloadDir(delaDir), datasetName);
    return dataset.exists();
  }

  public static List<AddressJSON> getBootstrap(List<ClusterAddressDTO> partners) {
    List<AddressJSON> result = new LinkedList<>();
    Gson gson = new Gson();
    for (ClusterAddressDTO p : partners) {
      AddressJSON adr = gson.fromJson(p.getDelaTransferAddress(), AddressJSON.class);
      result.add(adr);
    }
    return result;
  }

  public static class Cmds {

    public static final String SERVICE = "service";
    public static final String SEARCH = "search";
    public static final String DOWNLOAD = "download";
    public static final String CONTENTS = "contents";
    public static final String DETAILS = "details";
    public static final String CANCEL = "cancel";

  }
}
