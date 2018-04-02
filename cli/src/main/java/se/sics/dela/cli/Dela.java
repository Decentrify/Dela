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

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import se.sics.dela.cli.dto.AddressJSON;
import se.sics.dela.cli.dto.ExtendedDetails;
import se.sics.dela.cli.dto.FileInfo;
import se.sics.dela.cli.dto.HDFSDetails;
import se.sics.dela.cli.dto.HDFSEndpoint;
import se.sics.dela.cli.dto.HDFSResource;
import se.sics.dela.cli.dto.ManifestJSON;
import se.sics.dela.cli.dto.TorrentDownloadDTO;
import se.sics.dela.cli.dto.TorrentId;
import se.sics.ktoolbox.httpsclient.WebClient;
import se.sics.ktoolbox.httpsclient.WebResponse;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Dela {

  public static class Targets {

    public static String LOCAL = "http://127.0.0.1:40003";
  }

  public static class WebPath {

    public static final String CONTACT = "/vod/endpoint";
    public static final String START_DOWNLOAD = "/torrent/hops/download/start/basic";
    public static final String ADVANCE_DOWNLOAD = "/torrent/hops/download/advance/basic";
  }

  public static class Ops {

    public static boolean contact(String delaVersion) {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp = client
          .setTarget(Dela.Targets.LOCAL)
          .setPath(Dela.WebPath.CONTACT)
          .setPayload(delaVersion)
          .doPost();
        AddressJSON result = resp.readContent(AddressJSON.class);
        return true;
      }
    }

    public static boolean download(String publicDSId, String torrentName, String libDir, List<AddressJSON> partners) {
      try (WebClient client = WebClient.httpsInstance()) {
        TorrentId torrentId = new TorrentId(publicDSId);
        HDFSEndpoint endpoint = new HDFSEndpoint();
        String downloadDir = libDir + File.separator + torrentName;
        HDFSResource resource = new HDFSResource(downloadDir, "manifest.json");
        TorrentDownloadDTO.Start start = new TorrentDownloadDTO.Start(torrentId, torrentName, -1, -1,
          resource, partners, endpoint);
        WebResponse resp;
        resp = client
          .setTarget(Dela.Targets.LOCAL)
          .setPath(Dela.WebPath.START_DOWNLOAD)
          .setPayload(start)
          .doPost();

        if (!resp.statusOk()) {
          return false;
        }
        try {
          ExtendedDetails ed = readDetails(downloadDir);
          TorrentDownloadDTO.Advance advance = new TorrentDownloadDTO.Advance(torrentId, endpoint, ed);

          resp = client
            .setTarget(Dela.Targets.LOCAL)
            .setPath(Dela.WebPath.ADVANCE_DOWNLOAD)
            .setPayload(advance)
            .doPost();
        } catch (IOException ex) {
          return false;
        } 

        return resp.statusOk();
      }
    }

    private static ExtendedDetails readDetails(String downloadDir) throws IOException {
      byte[] jsonByte = Files.readAllBytes(Paths.get(downloadDir, "manifest.json"));
      String jsonString = new String(jsonByte, "UTF-8");
      ManifestJSON manifest = new Gson().fromJson(jsonString, ManifestJSON.class);
      List<HDFSDetails> details = new LinkedList<>();
      for(FileInfo fi : manifest.getFileInfos()) {
        String fileName = fi.getFileName();
        HDFSResource fileResource = new HDFSResource(downloadDir, fileName);
        details.add(new HDFSDetails(fileName, fileResource));
      }
      return new ExtendedDetails(details);
    }
  }
}
