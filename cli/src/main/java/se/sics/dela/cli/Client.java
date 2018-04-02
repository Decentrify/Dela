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
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import se.sics.dela.cli.cmd.DownloadCmd;
import se.sics.dela.cli.cmd.SearchCmd;
import se.sics.dela.cli.dto.AddressJSON;
import se.sics.dela.cli.dto.ClusterAddressDTO;
import se.sics.dela.cli.dto.SearchServiceDTO;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Client {

  private static Map<String, Object> cmds = new HashMap<>();

  public static void main(String[] args) {
    JCommander jc = registerCmds();
    try {
      jc.parse(args);
      executeCmd(jc.getParsedAlias());
    } catch (MissingCommandException ex) {
      jc.usage();
    }
  }

  private static JCommander registerCmds() {
    JCommander.Builder jcb = JCommander.newBuilder();

    SearchCmd searchCmd = new SearchCmd();
    cmds.put(Cmds.SEARCH, searchCmd);
    jcb.addCommand(Cmds.SEARCH, searchCmd);
    
    DownloadCmd downloadCmd = new DownloadCmd();
    cmds.put(Cmds.DOWNLOAD, downloadCmd);
    jcb.addCommand(Cmds.DOWNLOAD, downloadCmd);

    return jcb.build();
  }

  private static void executeCmd(String cmdName) {
    switch (cmdName) {
      case Cmds.SEARCH: {
        SearchCmd cmd = (SearchCmd) cmds.get(Cmds.SEARCH);
        SearchServiceDTO.Item[] items = Tracker.Ops.search(cmd.target, cmd.term);
        Tracker.Printers.searchResultConsolePrinter().accept(items);
      }
      break;
      case Cmds.DOWNLOAD: {
        String libDir = "/Users/Alex/Documents/_Work/Code/decentrify/run/dela_cli/library";
        DownloadCmd cmd = (DownloadCmd) cmds.get(Cmds.DOWNLOAD);
        String delaVersion = "0.0.3";
        Dela.Ops.contact(delaVersion);
        SearchServiceDTO.ItemDetails datasetDetails = Tracker.Ops.datasetDetails(cmd.target, cmd.datasetId);
        Dela.Ops.download(cmd.datasetId, cmd.datasetName, libDir, getBootstrap(datasetDetails.getBootstrap()));
        System.out.println("done");
      }
    }
  }
  
  public static List<AddressJSON> getBootstrap(List<ClusterAddressDTO> partners) {
    List<AddressJSON> result = new LinkedList<>();
    Gson gson = new Gson();
    for(ClusterAddressDTO p : partners) {
      AddressJSON adr = gson.fromJson(p.getDelaTransferAddress(), AddressJSON.class);
      //port forwarding test hack - remove
      adr.setIp("127.0.0.1"); 
      result.add(adr);
    }
    return result;
  }

  public static class Cmds {

    public static final String SEARCH = "search";
    public static final String DOWNLOAD = "download";
  }

}
