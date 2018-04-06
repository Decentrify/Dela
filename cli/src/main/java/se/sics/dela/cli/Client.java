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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
      executeCmd(out, jc.getParsedAlias());
    } catch(MissingCommandException ex) {
      out.write(ex.getMessage());
      StringBuilder sb = new StringBuilder();
      jc.usage(sb);
      out.write(sb.toString());
    } catch (ParameterException ex) {
      out.write(ex.getMessage());
      StringBuilder sb = new StringBuilder();
      jc.usage(jc.getParsedCommand(), sb);
      out.write(sb.toString());
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

  private static void executeCmd(PrintWriter out, String cmdName) {
    switch (cmdName) {
      case Cmds.SERVICE: {
        try {
          Dela.Ops.contact(delaVersion());
          out.write("service online\n");
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
        }
      }
      break;
      case Cmds.SEARCH: {
        SearchCmd cmd = (SearchCmd) cmds.get(Cmds.SEARCH);
        try {
          SearchServiceDTO.Item[] items = Tracker.Ops.search(cmd.target, cmd.term);
          Tracker.Printers.searchResultConsolePrinter(out).accept(items);
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        }
      }
      break;
      case Cmds.DOWNLOAD: {
        DownloadCmd cmd = (DownloadCmd) cmds.get(Cmds.DOWNLOAD);
        try {
          Dela.Ops.contact(delaVersion());
          SearchServiceDTO.ItemDetails datasetDetails = Tracker.Ops.datasetDetails(cmd.target, cmd.datasetId);
          String datasetName = (cmd.datasetName == null) ? cmd.datasetId : cmd.datasetName;
          Dela.Ops.download(cmd.datasetId, datasetName, libDir(), getBootstrap(datasetDetails.getBootstrap()));
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
        }
      }
      break;
      case Cmds.CONTENTS: {
        try {
          Dela.Ops.contact(delaVersion());
          ContentsCmd cmd = (ContentsCmd) cmds.get(Cmds.CONTENTS);
          HopsContentsSummaryJSON.Hops contents = Dela.Ops.contents();
          Dela.Printers.contentsConsolePrinter(out).accept(contents);
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
        }
      }
      break;
      case Cmds.DETAILS: {
        try {
          Dela.Ops.contact(delaVersion());
          DetailsCmd cmd = (DetailsCmd) cmds.get(Cmds.DETAILS);
          TorrentExtendedStatusJSON torrent = Dela.Ops.details(cmd.datasetId);
          Dela.Printers.detailsConsolePrinter(out).accept(torrent);
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
        }
      }
      break;
      case Cmds.CANCEL: {
        try {
          Dela.Ops.contact(delaVersion());
          CancelCmd cmd = (CancelCmd) cmds.get(Cmds.CANCEL);
          Dela.Ops.cancel(cmd.datasetId);
        } catch (UnknownClientException ex) {
          ex.printStackTrace(out);
        } catch (ManagedClientException ex) {
          out.write(ex.getMessage() + "\n");
        }
      }
      break;
    }
    out.flush();
  }

  private static String delaVersion() {
    return "0.0.3";
  }

  private static String libDir() {
    return "/Users/Alex/Documents/_Work/Code/decentrify/run/dela_cli/library";
  }

  public static List<AddressJSON> getBootstrap(List<ClusterAddressDTO> partners) {
    List<AddressJSON> result = new LinkedList<>();
    Gson gson = new Gson();
    for (ClusterAddressDTO p : partners) {
      AddressJSON adr = gson.fromJson(p.getDelaTransferAddress(), AddressJSON.class);
      //port forwarding test hack - remove
//      adr.setIp("127.0.0.1"); 
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
