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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.ws.rs.ProcessingException;
import se.sics.dela.cli.dto.AddressJSON;
import se.sics.dela.cli.dto.ElementSummaryJSON;
import se.sics.dela.cli.dto.ErrorDescDTO;
import se.sics.dela.cli.dto.HDFSEndpoint;
import se.sics.dela.cli.dto.HDFSResource;
import se.sics.dela.cli.dto.HopsContentsReqJSON;
import se.sics.dela.cli.dto.HopsContentsSummaryJSON;
import se.sics.dela.cli.dto.TorrentDownloadDTO;
import se.sics.dela.cli.dto.TorrentExtendedStatusJSON;
import se.sics.dela.cli.dto.TorrentIdJSON;
import se.sics.dela.cli.util.ManagedClientException;
import se.sics.dela.cli.util.UnknownClientException;
import se.sics.ktoolbox.httpsclient.WebClient;
import se.sics.ktoolbox.httpsclient.WebResponse;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class Dela {

  
  private static Config config(String delaDir) throws ManagedClientException {
    String delaConfigDir = delaDir + File.separator + "config";
    File delaConfigFile = new File(delaConfigDir, "application.conf");
    if (!delaConfigFile.exists()) {
      throw new ManagedClientException("no dela config file found at:" + delaConfigDir);
    }
    Config delaConfig = ConfigFactory.parseFile(delaConfigFile);
    return delaConfig;
  }

  public static class Targets {

    public static String local(String delaDir) throws ManagedClientException {
      Config delaConfig = Dela.config(delaDir);
      String port = delaConfig.getString("http.port");
      return "http://localhost:" + port;
    }
  }

  public static String version(String delaDir) throws ManagedClientException {
    Config delaConfig = Dela.config(delaDir);
    String version = delaConfig.getString("version");
    return version;
  }

  public static class WebPath {

    public static final String CONTACT = "/vod/endpoint";
    public static final String DOWNLOAD = "/torrent/hops/download/start/basic";
    public static final String CONTENTS = "/library/hopscontents";
    public static final String DETAILS = "/library/extended";
    public static final String CANCEL = "/torrent/hops/stop";
  }

  public static class Printers {

    public static Consumer<HopsContentsSummaryJSON.Hops> contentsConsolePrinter(final PrintWriter out) {
      return new Consumer<HopsContentsSummaryJSON.Hops>() {

        @Override
        public void accept(HopsContentsSummaryJSON.Hops items) {
          out.printf("%20s", "DatasetId");
          out.printf(" | %11s", "Status");
          out.printf(" | DatasetName\n");
          for (HopsContentsSummaryJSON.ContentsElement projectItems : items.getContents()) {
            for (ElementSummaryJSON item : projectItems.projectContents) {
              datasetIdFormat(out, item.getTorrentId().getVal());
              out.printf(" | %11s", item.getTorrentStatus());
              out.printf(" | %s", item.getFileName());
              out.printf("\n");
            }
          }
        }
      };
    }

    public static Consumer<TorrentExtendedStatusJSON> detailsConsolePrinter(final PrintWriter out) {
      return new Consumer<TorrentExtendedStatusJSON>() {

        @Override
        public void accept(TorrentExtendedStatusJSON item) {
          out.printf("%20s", "DatasetId");
          out.printf(" | %11s", "Status");
          out.printf(" | Completed | DownloadSpeed\n");
          datasetIdFormat(out, item.getTorrentId().getVal());
          out.printf(" | %11s", item.getTorrentStatus());
          if (item.getTorrentStatus().equals("DOWNLOADING")) {
            out.printf(" | %8s%%", Math.round(item.getPercentageCompleted()));
            downloadSpeed(out, item.getDownloadSpeed());
          }
          out.printf("\n");
        }
      };
    }

    private static void datasetIdFormat(PrintWriter out, String datasetId) {
      if (datasetId.length() < 20) {
        out.printf("%20s", datasetId);
      } else if (datasetId.length() < 50) {
        out.printf("%50s", datasetId);
      } else if (datasetId.length() < 100) {
        out.printf("%100s", datasetId);
      } else {
        out.printf(datasetId);
      }
    }

    private static void downloadSpeed(PrintWriter out, long downloadSpeed) {
      DecimalFormat f = new DecimalFormat("##.00");
      if (downloadSpeed < 1024) {
        out.printf(" | %9s B/s", downloadSpeed);
      } else if (downloadSpeed < 1024 * 1024) {
        String speed = f.format((double) downloadSpeed / 1024);
        out.printf(" | %8s KB/s", speed);
      } else if (downloadSpeed < 1024 * 1024 * 1024) {
        String speed = f.format((double) downloadSpeed / (1024 * 1024));
        out.printf(" | %8s MB/s", speed);
      }
    }
  }

  public static class Ops {

    public static boolean delaVersion(String delaDir, String trackerDelaVersion) throws ManagedClientException {
      return Dela.version(delaDir).equals(trackerDelaVersion);
    }

    public static void contact(String delaDir) throws UnknownClientException, ManagedClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp = client
          .setTarget(Dela.Targets.local(delaDir))
          .setPath(Dela.WebPath.CONTACT)
          .setPayload(Dela.version(delaDir))
          .doPost();
        if (!resp.statusOk()) {
          Optional<ErrorDescDTO> errorDesc = getErrorDesc(resp);
          if (errorDesc.isPresent()) {
            throw new UnknownClientException("dela contact download failed with:" + errorDesc.get().getDetails());
          } else {
            throw new UnknownClientException("dela contact download failed with status:" + resp.response.getStatus());
          }
        }
        AddressJSON result = resp.readContent(AddressJSON.class);
      } catch (UnknownClientException ex) {
        throw ex;
      } catch (ProcessingException ex) {
        if (ex.getCause() != null && ex.getCause() instanceof ConnectException) {
          throw new ManagedClientException("service offline");
        } else {
          throw new UnknownClientException(ex);
        }
      } catch (Exception ex) {
        throw new UnknownClientException(ex);
      }
    }

    public static void download(String delaDir, String libDir, String publicDSId, String torrentName, 
      List<AddressJSON> partners)
      throws UnknownClientException, ManagedClientException {
      TorrentIdJSON torrentId = new TorrentIdJSON(publicDSId);
      HDFSEndpoint endpoint = new HDFSEndpoint();
      String downloadDir = libDir + File.separator + torrentName;
      HDFSResource resource = new HDFSResource(downloadDir, "manifest.json");
      TorrentDownloadDTO.Start req = new TorrentDownloadDTO.Start(torrentId, torrentName, -1, -1,
        resource, partners, endpoint);
      WebResponse resp;

      try (WebClient client = WebClient.httpsInstance()) {

        resp = client
          .setTarget(Dela.Targets.local(delaDir))
          .setPath(Dela.WebPath.DOWNLOAD)
          .setPayload(req)
          .doPost();
        if (!resp.statusOk()) {
          Optional<ErrorDescDTO> errorDesc = getErrorDesc(resp);
          if (errorDesc.isPresent()) {
            if (torrentActiveError(errorDesc.get())) {
              throw new ManagedClientException("torrent: " + req.getTorrentId().getVal() + " already active");
            } else {
              throw new UnknownClientException("dela download failed with:" + errorDesc.get().getDetails());
            }
          } else {
            throw new UnknownClientException("dela download failed with status" + resp.response.getStatus());
          }
        }
      } catch (UnknownClientException | ManagedClientException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new UnknownClientException(ex);
      }
    }

    public static HopsContentsSummaryJSON.Hops contents(String delaDir) throws UnknownClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp;
        HopsContentsReqJSON req = new HopsContentsReqJSON();
        try {
          resp = client
            .setTarget(Dela.Targets.local(delaDir))
            .setPath(Dela.WebPath.CONTENTS)
            .setPayload(req)
            .doPost();
          if (!resp.statusOk()) {
            Optional<ErrorDescDTO> errorDesc = getErrorDesc(resp);
            if (errorDesc.isPresent()) {
              throw new UnknownClientException("dela contents failed with:" + resp.response.getStatus());
            } else {
              throw new UnknownClientException("dela contents failed with status" + resp.response.getStatus());
            }
          }
          HopsContentsSummaryJSON.Hops result = resp.readContent(HopsContentsSummaryJSON.Hops.class);
          return result;
        } catch (UnknownClientException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new UnknownClientException(ex);
        }
      }
    }

    public static TorrentExtendedStatusJSON details(String delaDir, String publicDSId) throws UnknownClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp;
        TorrentIdJSON req = new TorrentIdJSON(publicDSId);
        try {
          resp = client
            .setTarget(Dela.Targets.local(delaDir))
            .setPath(Dela.WebPath.DETAILS)
            .setPayload(req)
            .doPost();
          if (!resp.statusOk()) {
            Optional<ErrorDescDTO> errorDesc = getErrorDesc(resp);
            if (errorDesc.isPresent()) {
              throw new UnknownClientException("dela transfer details failed with:" + resp.response.getStatus());
            } else {
              throw new UnknownClientException("dela transfer details failed with status" + resp.response.getStatus());
            }
          }
          TorrentExtendedStatusJSON result = resp.readContent(TorrentExtendedStatusJSON.class);
          return result;
        } catch (UnknownClientException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new UnknownClientException(ex);
        }
      }
    }

    public static void cancel(String delaDir, String publicDSId) throws UnknownClientException {
      try (WebClient client = WebClient.httpsInstance()) {
        WebResponse resp;
        TorrentIdJSON req = new TorrentIdJSON(publicDSId);
        try {
          resp = client
            .setTarget(Dela.Targets.local(delaDir))
            .setPath(Dela.WebPath.CANCEL)
            .setPayload(req)
            .doPost();
          if (!resp.statusOk()) {
            Optional<ErrorDescDTO> errorDesc = getErrorDesc(resp);
            if (errorDesc.isPresent()) {
              throw new UnknownClientException("dela transfer cancel failed with:" + resp.response.getStatus());
            } else {
              throw new UnknownClientException("dela transfer cancel failed with status" + resp.response.getStatus());
            }
          }
        } catch (UnknownClientException ex) {
          throw ex;
        } catch (Exception ex) {
          throw new UnknownClientException(ex);
        }
      }
    }

    private static boolean torrentActiveError(ErrorDescDTO errorDesc) {
      if (errorDesc.getDetails().endsWith("active already")) {
        return true;
      }
      return false;
    }

    private static Optional<ErrorDescDTO> getErrorDesc(WebResponse resp) {
      try {
        ErrorDescDTO errorDetails = resp.readErrorDetails(ErrorDescDTO.class);
        return Optional.of(errorDetails);
      } catch (IllegalStateException ex) {
        return Optional.empty();
      }
    }
  }
}
