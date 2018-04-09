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
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Client {

  private static Map<String, Object> cmds = new HashMap<>();

  public static void main(String[] args) {
    JCommander jc = registerCmds();
    PrintWriter out = new PrintWriter(System.out);
    try {
      jc.parse(args);
      int retVal = executeCmd(out, jc.getParsedAlias());
      out.flush();
      if(retVal != 0) {
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
      StringBuilder sb = new StringBuilder();
      jc.usage(jc.getParsedCommand(), sb);
      out.write(sb.toString());
      out.flush();
      System.exit(-1);
    } finally {
      out.flush();
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

  private static int executeCmd(PrintWriter out, String cmdName) {
    switch (cmdName) {
      case Cmds.SERVICE: {
        try {
          Dela.Ops.contact();
          out.write("service online\n");
          return 0;
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
          SearchServiceDTO.Item[] items = Tracker.Ops.search(cmd.target, cmd.term);
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
          Dela.Ops.contact();
          SearchServiceDTO.ItemDetails trackerDetails = Tracker.Ops.datasetDetails(cmd.target, cmd.datasetId);
          TorrentExtendedStatusJSON delaDetails = Dela.Ops.details(cmd.datasetId);
          if (delaDetails.getTorrentStatus().equals("NONE")) {
            String libDir = delaLib();
            String datasetName = getDatasetName(out, cmd);
            out.printf("Library: %s \n", libDir);
            out.printf("Saving dataset with id: %s as: %s \n", cmd.datasetId, datasetName);
            Dela.Ops.download(cmd.datasetId, datasetName, delaLib(), getBootstrap(trackerDetails.getBootstrap()));
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
          Dela.Ops.contact();
          ContentsCmd cmd = (ContentsCmd) cmds.get(Cmds.CONTENTS);
          HopsContentsSummaryJSON.Hops contents = Dela.Ops.contents();
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
          Dela.Ops.contact();
          DetailsCmd cmd = (DetailsCmd) cmds.get(Cmds.DETAILS);
          TorrentExtendedStatusJSON torrent = Dela.Ops.details(cmd.datasetId);
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
          Dela.Ops.contact();
          CancelCmd cmd = (CancelCmd) cmds.get(Cmds.CANCEL);
          Dela.Ops.cancel(cmd.datasetId);
          return 0;
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
          return -1;
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
          return -1;
        }
      }
      default: return -1;
    }
  }

  private static String getDatasetName(PrintWriter out, DownloadCmd cmd) throws ManagedClientException {
    String libDir = delaLib();
    String datasetName;
    if (cmd.datasetName != null) {
      datasetName = cmd.datasetName;
      if (datasetExists(libDir, datasetName)) {
        StringBuilder sb = new StringBuilder();
        sb.append("Folder for dataset: ").append(cmd.datasetName);
        sb.append("already exists in library: ").append(libDir);
        sb.append("\nDelete folder or rename dataset.");
        throw new ManagedClientException(sb.toString());
      }
    } else {
      datasetName = cmd.datasetId;
      Random rand = new Random();
      while (datasetExists(libDir, datasetName)) {
        datasetName = cmd.datasetId + "_" + rand.nextInt(Integer.MAX_VALUE);
      }
    }
    return datasetName;
  }

  private static boolean datasetExists(String lib, String datasetName) {
    File dataset = new File(lib, datasetName);
    return dataset.exists();
  }

  private static String delaLib() {
    return System.getenv("DELA_LIB");
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
