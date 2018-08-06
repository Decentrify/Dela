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

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import static se.sics.dela.cli.Transfer.Setup.delaDownloadDir;
import se.sics.dela.cli.cmd.DownloadCmd;
import se.sics.dela.cli.dto.transfer.AddressJSON;
import se.sics.dela.cli.dto.ElementSummaryJSON;
import se.sics.dela.cli.dto.HDFSEndpoint;
import se.sics.dela.cli.dto.HDFSResource;
import se.sics.dela.cli.dto.HopsContentsReqJSON;
import se.sics.dela.cli.dto.HopsContentsSummaryJSON;
import se.sics.dela.cli.dto.transfer.TorrentDownloadDTO;
import se.sics.dela.cli.dto.TorrentExtendedStatusJSON;
import se.sics.dela.cli.dto.TorrentIdJSON;
import se.sics.ktoolbox.webclient.WebClient;
import se.sics.ktoolbox.webclient.WebClient.ClientException;
import static se.sics.ktoolbox.webclient.WebResponse.readContent;
import se.sics.ktoolbox.util.trysf.Try;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc2;
import se.sics.dela.cli.dto.tracker.SearchServiceDTO;
import se.sics.dela.cli.dto.transfer.SuccessJSON;
import se.sics.dela.cli.util.ExHelper;
import se.sics.dela.cli.util.ExHelper.DelaException;
import static se.sics.dela.cli.util.ExHelper.simpleDelaExMapper;
import se.sics.ktoolbox.util.trysf.TryHelper.Joiner;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFFail;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc0;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc1;
import static se.sics.ktoolbox.util.trysf.TryHelper.tryFSucc3;

public class Transfer {

  public static class Target {

    public static Try<String> delaClient(String delaDir) {
      Try<Config> delaConfig = Setup.config(delaDir);
      if (delaConfig.isFailure()) {
        return (Try.Failure) delaConfig;
      }
      String port = delaConfig.get().getString("http.port");
      return new Try.Success("http://localhost:" + port);
    }
  }

  public static class WebPath {

    public static final String CONTACT = "/vod/endpoint";
    public static final String DOWNLOAD = "/torrent/hops/download/start/basic";
    public static final String CONTENTS = "/library/hopscontents";
    public static final String DETAILS = "/library/extended";
    public static final String CANCEL = "/torrent/hops/stop";
  }

  public static class Printer {

    public static <O> BiFunction<O, Throwable, Try<String>> statusOK() {
      return tryFSucc0(() -> {
        return new Try.Success("dela service: running");
      });
    }

    public static <O> BiFunction<O, Throwable, Try<String>> statusFail() {
      return tryFFail((Throwable ex) -> {
        if (ex instanceof WebClient.CommunicationException) {
          return new Try.Success("dela service: not running");
        }
        return new Try.Failure(ex);
      });
    }

    public static BiFunction<PrintWriter, Triplet<String, TorrentExtendedStatusJSON, SuccessJSON>, PrintWriter>
      delaDownloadDetailsPrinter(String delaDir, String datasetId) {
      return (PrintWriter out, Triplet<String, TorrentExtendedStatusJSON, SuccessJSON> details) -> {
        TorrentExtendedStatusJSON downloadDetails = details.getValue1();
        String datasetName = details.getValue0();
        String resultDetails = details.getValue2().getDetails();
        if (downloadDetails.getTorrentStatus().equals("NONE")) {
          out.printf("Dela home directory: %s \n", delaDir);
          out.printf("Saving dataset with id: %s as: %s \n", datasetId, datasetName);
          out.println(resultDetails);
        } else {
          out.printf("Dataset with id: %s already active \n", datasetId);
        }
        return out;
      };
    }

    public static BiFunction<PrintWriter, HopsContentsSummaryJSON.Hops, PrintWriter> delaContentsPrinter() {
      return (PrintWriter out, HopsContentsSummaryJSON.Hops contents) -> {
        out.printf("%20s", "DatasetId");
        out.printf(" | %11s", "Status");
        out.printf(" | DatasetName\n");
        for (HopsContentsSummaryJSON.ContentsElement project : contents.getContents()) {
          for (ElementSummaryJSON dataset : project.projectContents) {
            datasetIdFormat(out, dataset.getTorrentId().getVal());
            out.printf(" | %11s", dataset.getTorrentStatus());
            out.printf(" | %s", dataset.getFileName());
            out.printf("\n");
          }
        }
        return out;
      };
    }

    public static BiFunction<PrintWriter, TorrentExtendedStatusJSON, PrintWriter> delaDatasetPrinter() {
      return (PrintWriter out, TorrentExtendedStatusJSON dataset) -> {
        out.printf("%20s", "DatasetId");
        out.printf(" | %11s", "Status");
        out.printf(" | Completed | DownloadSpeed\n");
        datasetIdFormat(out, dataset.getTorrentId().getVal());
        out.printf(" | %11s", dataset.getTorrentStatus());
        if (dataset.getTorrentStatus().equals("DOWNLOADING")) {
          out.printf(" | %8s%%", Math.round(dataset.getPercentageCompleted()));
          downloadSpeed(out, dataset.getDownloadSpeed());
        }
        out.printf("\n");
        return out;
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

  public static class Rest {

    public static BiFunction<Pair<String, String>, Throwable, Try<AddressJSON>> contact() {
      return tryFSucc2((String delaVersion) -> (String delaClient) -> {
        try (WebClient client = WebClient.httpsInstance()) {
          Try<AddressJSON> result = client
            .setTarget(delaClient)
            .setPath(Transfer.WebPath.CONTACT)
            .setPayload(delaVersion)
            .tryPost()
            .flatMap(readContent(AddressJSON.class, simpleDelaExMapper()));
          return result;
        }
      });
    }

    public static BiFunction<Triplet<String, String, List<AddressJSON>>, Throwable, Try<SuccessJSON>>
      delaDownload(String delaDir, String publicDSId) {
      return tryFSucc3((String delaClient) -> (String datasetName) -> (List<AddressJSON> partners) -> {
        TorrentIdJSON torrentId = new TorrentIdJSON(publicDSId);
        HDFSEndpoint endpoint = new HDFSEndpoint();
        HDFSResource resource = new HDFSResource(delaDownloadDir(delaDir, datasetName), "manifest.json");
        TorrentDownloadDTO.Start req = new TorrentDownloadDTO.Start(torrentId, datasetName, -1, -1,
          resource, partners, endpoint);

        try (WebClient client = WebClient.httpsInstance()) {

          Try<SuccessJSON> result = client
            .setTarget(delaClient)
            .setPath(Transfer.WebPath.DOWNLOAD)
            .setPayload(req)
            .tryPost()
            .flatMap(readContent(SuccessJSON.class, simpleDelaExMapper()))
            .recoverWith(Recover.torrentActive());
          return result;
        }
      });
    }

    public static BiFunction<String, Throwable, Try<HopsContentsSummaryJSON.Hops>> delaContents() {
      return tryFSucc1((String delaClient) -> {
        try (WebClient client = WebClient.httpsInstance()) {
          HopsContentsReqJSON req = new HopsContentsReqJSON();
          Try<HopsContentsSummaryJSON.Hops> result = client
            .setTarget(delaClient)
            .setPath(Transfer.WebPath.CONTENTS)
            .setPayload(req)
            .tryPost()
            .flatMap(readContent(HopsContentsSummaryJSON.Hops.class, simpleDelaExMapper()));
          return result;
        }
      });
    }

    public static BiFunction<String, Throwable, Try<TorrentExtendedStatusJSON>> delaDatasetDetails(String publicDSId) {
      return tryFSucc1((String delaClient) -> {
        try (WebClient client = WebClient.httpsInstance()) {
          Try<TorrentExtendedStatusJSON> result = client
            .setTarget(delaClient)
            .setPath(Transfer.WebPath.DETAILS)
            .setPayload(new TorrentIdJSON(publicDSId))
            .tryPost()
            .flatMap(readContent(TorrentExtendedStatusJSON.class, simpleDelaExMapper()));
          return result;
        }
      });
    }

    public static BiFunction<String, Throwable, Try<SuccessJSON>> delaDatasetCancel(String publicDSId) {
      return tryFSucc1((String delaClient) -> {
        try (WebClient client = WebClient.httpsInstance()) {
          TorrentIdJSON req = new TorrentIdJSON(publicDSId);
          Try<SuccessJSON> result = client
            .setTarget(delaClient)
            .setPath(Transfer.WebPath.CANCEL)
            .setPayload(req)
            .tryPost()
            .flatMap(readContent(SuccessJSON.class, simpleDelaExMapper()));
          return result;
        }
      });
    }
  }

  public static class Daemon {

    public static BiFunction<String, Throwable, Try<String>>
      start(PrintWriter out, boolean isWindows, String delaDir) {
      return tryFSucc0(() -> {
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
          return new Try.Failure(new ClientException("windows not supported yet"));
        } else {
          builder.command("sh", "-c", "./bin/daemon_start");
        }
        builder.directory(new File(delaDir));

        try {
          Process process = builder.start();
          StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream(), out::println);
          ExecutorService es = Executors.newSingleThreadExecutor();
          Future f = es.submit(outStreamGobbler);
          if (process.waitFor() != 0) {
            return new Try.Failure(new ClientException("daemon start: fail"));
          }
          f.get();
          es.shutdown();
          out.flush();
          return new Try.Success("daemon start: success");
        } catch (InterruptedException | ExecutionException | IOException ex) {
          return new Try.Failure(new ClientException(ex));
        }
      });
    }

    public static BiFunction<String, Throwable, Try<String>>
      stop(PrintWriter out, boolean isWindows, String delaDir) {
      return tryFSucc0(() -> {
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
          return new Try.Failure(new ClientException("windows not supported yet"));
        } else {
          builder.command("sh", "-c", "./bin/daemon_stop");
        }
        builder.directory(new File(delaDir));

        try {
          Process process = builder.start();
          StreamGobbler outStreamGobbler = new StreamGobbler(process.getInputStream(), out::println);
          ExecutorService es = Executors.newSingleThreadExecutor();
          Future f = es.submit(outStreamGobbler);
          if (process.waitFor() != 0) {
            return new Try.Failure(new ClientException("daemon stop: fail"));
          }
          f.get();
          es.shutdown();
          out.flush();
          return new Try.Success("daemon stop: success");
        } catch (InterruptedException | ExecutionException | IOException ex) {
          return new Try.Failure(new ClientException(ex));
        }
      });
    }

    public static class StreamGobbler implements Runnable {

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
  }

  public static class Util {

    public static BiFunction<SearchServiceDTO.ItemDetails, Throwable, List<AddressJSON>> bootstrap() {
      return tryFSucc1((SearchServiceDTO.ItemDetails details) -> {
        List<AddressJSON> result = new LinkedList<>();
        Gson gson = new Gson();
        details.getBootstrap().forEach((p) -> {
          result.add(gson.fromJson(p.getDelaTransferAddress(), AddressJSON.class));
        });
        return result;
      });
    }

    public static Try<String> checkDelaVersion(String delaDir, PrintWriter out) {
      Try<String> localDelaVersion = Setup.version(delaDir);
      Try<String> trackerDelaVersion = Tracker.Rest.delaVersion();
      Try<String> result = Joiner.combine(localDelaVersion, trackerDelaVersion)
        .flatMap(processVersion());
      return result;
    }

    private static BiFunction<Pair<String, String>, Throwable, Try<String>> processVersion() {
      return tryFSucc2((String localDelaVersion) -> (String trackerDelaVersion) -> {
        String[] v1 = trackerDelaVersion.split("\\.");
        String[] v2 = localDelaVersion.split("\\.");
        if (v1.length != v2.length || v1.length != 3) {
          String cause = "WARNING! Version structure mismatch."
            + " Local:" + localDelaVersion
            + " - Tracker:" + trackerDelaVersion;
          return new Try.Failure(new WebClient.ClientException(cause));
        }
        if (!v1[0].equals(v2[0]) || !v1[1].equals(v2[1])) {
          String cause = "WARNING! Version incompatible."
            + " Local:" + localDelaVersion
            + " - Tracker:" + trackerDelaVersion;
          return new Try.Failure(new WebClient.ClientException(cause));
        }
        if (!v1[2].equals(v2[2])) {
          String cause = "Patch version mismatch. Protocol still be compatible."
            + " Local:" + localDelaVersion
            + " - Tracker:" + trackerDelaVersion;
          return new Try.Failure(new WebClient.ClientException(cause));
        }
        return new Try.Success(localDelaVersion);
      });
    }
  }

  public static class Setup {

    private static Try<Config> config(String delaDir) {
      String delaConfigDir = delaDir + File.separator + "conf";
      File delaConfigFile = new File(delaConfigDir, "application.conf");
      if (!delaConfigFile.exists()) {
        return new Try.Failure(new ClientException("no dela config file found at:" + delaConfigDir));
      }
      Config delaConfig = ConfigFactory.parseFile(delaConfigFile);
      return new Try.Success(delaConfig);
    }

    public static Try<String> version(String delaDir) {
      Try<Config> delaConfig = config(delaDir);
      if (delaConfig.isFailure()) {
        return (Try.Failure) delaConfig;
      }
      String version = delaConfig.get().getString("version");
      return new Try.Success(version);
    }

    public static Try<String> datasetName(String delaDir, DownloadCmd cmd) {
      String datasetName;
      if (cmd.datasetName != null) {
        datasetName = cmd.datasetName;
        if (datasetExists(delaDir, datasetName)) {
          StringBuilder sb = new StringBuilder();
          sb.append("Folder for dataset: ").append(cmd.datasetName);
          sb.append("already exists in library");
          sb.append("\nDelete folder or rename dataset.");
          return new Try.Failure(new ClientException(sb.toString()));
        }
      } else {
        datasetName = cmd.datasetId;
        Random rand = new Random();
        while (datasetExists(delaDir, datasetName)) {
          datasetName = cmd.datasetId + "_" + rand.nextInt(Integer.MAX_VALUE);
        }
      }
      return new Try.Success(datasetName);
    }

    private static boolean datasetExists(String delaDir, String datasetName) {
      File dataset = new File(delaDownloadDir(delaDir, datasetName));
      return dataset.exists();
    }

    public static String delaDownloadDir(String delaDir, String datasetDir) {
      return delaDir 
        + File.separator + "download"
        + File.separator + datasetDir;
    }
  }

  public static class Translate {

    public static boolean notReady(Try delaContact) {
      if (delaContact.isSuccess()) {
        return false;
      }
      try {
        ((Try.Failure) delaContact).checkedGet();
      } catch(Throwable ex) {
        if(ex instanceof DelaException) {
          DelaException delaEx = (DelaException)ex;
          if("vod not ready".equals(delaEx.details.getDetails())) {
            return true;
          }
        }
      }
      return false;
    }
  }

  public static class Recover {

    public static <O> BiFunction<O, Throwable, Try<SuccessJSON>> torrentActive() {
      return tryFFail((Throwable ex) -> {
        if (ex instanceof ExHelper.DelaException) {
          ExHelper.DelaException delaEx = (ExHelper.DelaException) ex;
          if (delaEx.details.getDetails().endsWith("active already")) {
            return new Try.Success(new SuccessJSON("torrent active"));
          }
        }
        return new Try.Failure(ex);
      });
    }
  }
}
