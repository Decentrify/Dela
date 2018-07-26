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
package se.sics.dozy.vod.system;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.SettableFuture;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.dozy.DozyResource;
import se.sics.dozy.DozyResult;
import se.sics.dozy.DozySyncComp;
import se.sics.dozy.DozySyncI;
import se.sics.dozy.dropwizard.DropwizardDela;
import se.sics.dozy.vod.DozyVoD;
import se.sics.dozy.vod.hops.torrent.HTAdvanceDownloadREST;
import se.sics.dozy.vod.hops.torrent.HTContentsREST;
import se.sics.dozy.vod.hops.torrent.HTStartDownloadREST;
import se.sics.dozy.vod.hops.torrent.HTStopREST;
import se.sics.dozy.vod.hops.torrent.HTUploadREST;
import se.sics.dozy.vod.library.TorrentExtendedStatusREST;
import se.sics.gvod.network.GVoDSerializerSetup;
import se.sics.kompics.Channel;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.config.Config;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.kompics.util.Identifiable;
import se.sics.ktoolbox.croupier.CroupierSerializerSetup;
import se.sics.ktoolbox.gradient.GradientSerializerSetup;
import se.sics.ktoolbox.netmngr.NetworkMngrSerializerSetup;
import se.sics.ktoolbox.netmngr.event.NetMngrReady;
import se.sics.ktoolbox.omngr.OMngrSerializerSetup;
import se.sics.ktoolbox.util.identifiable.BasicIdentifiers;
import se.sics.ktoolbox.util.identifiable.IdentifierFactory;
import se.sics.ktoolbox.util.identifiable.IdentifierRegistry;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayRegistry;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.setup.BasicSerializerSetup;
import se.sics.ktoolbox.util.status.Status;
import se.sics.ktoolbox.util.status.StatusPort;
import se.sics.ledbat.LedbatSerializerSetup;
import se.sics.nat.mngr.SimpleNatMngrComp;
import se.sics.nat.stun.StunSerializerSetup;
import se.sics.nstream.TorrentIds;
import se.sics.nstream.hops.SystemOverlays;
import se.sics.nstream.hops.libmngr.fsm.LibTFSM;
import se.sics.nstream.hops.library.HopsLibraryProvider;
import se.sics.nstream.hops.library.HopsTorrentPort;
import se.sics.nstream.hops.library.event.core.HopsTorrentDownloadEvent;
import se.sics.nstream.hops.library.event.core.HopsTorrentStopEvent;
import se.sics.nstream.hops.library.event.core.HopsTorrentUploadEvent;
import se.sics.nstream.library.LibraryMngrComp;
import se.sics.nstream.library.SystemPort;
import se.sics.nstream.library.event.system.SystemAddressEvent;
import se.sics.nstream.library.event.torrent.HopsContentsEvent;
import se.sics.nstream.library.event.torrent.TorrentExtendedStatusEvent;
import se.sics.nstream.storage.durable.DEndpointCtrlPort;
import se.sics.nstream.storage.durable.DStorageMngrComp;
import se.sics.nstream.storage.durable.DStoragePort;
import se.sics.nstream.storage.durable.DStreamControlPort;
import se.sics.nstream.torrent.TorrentMngrComp;
import se.sics.nstream.torrent.TorrentMngrPort;
import se.sics.nstream.torrent.tracking.TorrentStatusPort;
import se.sics.nstream.torrent.transfer.TransferCtrlPort;
import se.sics.nstream.util.CoreExtPorts;

public class VoDNatLauncher extends ComponentDefinition {

  private static Logger LOG = LoggerFactory.getLogger(VoDNatLauncher.class);
  private String logPrefix = "";

  //*****************************CONNECTIONS**********************************
  //********************INTERNAL_DO_NOT_CONNECT_TO****************************
  private Positive<StatusPort> otherStatusPort = requires(StatusPort.class);
  //****************************EXTERNAL_STATE********************************
  private KAddress selfAdr;
  //****************************INTERNAL_STATE********************************
  private Component timerComp;
  private Component networkMngrComp;
  private Component libraryMngrComp;
  private Component torrentMngrComp;
  private Component storageMngrComp;
  private Component systemSyncIComp;
  private Component hopsTorrentSyncIComp;
  private final Init init;
  //**************************************************************************

  public VoDNatLauncher(Init init) {
    this.init = init;
    LOG.info("{}starting...", logPrefix);

    subscribe(handleStart, control);
    subscribe(handleNetReady, otherStatusPort);

  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting", logPrefix);

      timerComp = create(JavaTimer.class, Init.NONE);
      setNetworkMngr();

      trigger(Start.event, timerComp.control());
      trigger(Start.event, networkMngrComp.control());
    }
  };

  private void setNetworkMngr() {
    LOG.info("{}setting up network mngr", logPrefix);
    SimpleNatMngrComp.ExtPort netExtPorts = new SimpleNatMngrComp.ExtPort(timerComp.getPositive(Timer.class));
    networkMngrComp = create(SimpleNatMngrComp.class, new SimpleNatMngrComp.Init(netExtPorts));
    connect(networkMngrComp.getPositive(StatusPort.class), otherStatusPort.getPair(), Channel.TWO_WAY);
  }

  ClassMatchedHandler handleNetReady
    = new ClassMatchedHandler<NetMngrReady, Status.Internal<NetMngrReady>>() {
    @Override
    public void handle(NetMngrReady content, Status.Internal<NetMngrReady> container) {
      LOG.info("{}network mngr ready", logPrefix);
      selfAdr = content.systemAdr;

      setStorageMngr();
      setTorrentMngr();
      setLibraryMngr();
      setSystemSyncI();
      setTorrentSyncI();

      trigger(Start.event, storageMngrComp.control());
      trigger(Start.event, torrentMngrComp.control());
      trigger(Start.event, libraryMngrComp.control());
      trigger(Start.event, systemSyncIComp.control());
      trigger(Start.event, hopsTorrentSyncIComp.control());

      LOG.info("{}starting complete...", logPrefix);
    }
  };

  private void setStorageMngr() {
    storageMngrComp = create(DStorageMngrComp.class, new DStorageMngrComp.Init(selfAdr.getId()));
    connect(storageMngrComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
  }

  private void setTorrentMngr() {
    torrentMngrComp = create(TorrentMngrComp.class, new TorrentMngrComp.Init(selfAdr));
    connect(torrentMngrComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(Network.class), networkMngrComp.getPositive(Network.class), Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(DStreamControlPort.class), storageMngrComp.getPositive(DStreamControlPort.class),
      Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(DStoragePort.class), storageMngrComp.getPositive(DStoragePort.class),
      Channel.TWO_WAY);
  }

  private void setLibraryMngr() {
    CoreExtPorts extPorts = new CoreExtPorts(timerComp.getPositive(Timer.class), networkMngrComp.getPositive(
      Network.class));
    libraryMngrComp = create(LibraryMngrComp.class, new LibraryMngrComp.Init(selfAdr, new HopsLibraryProvider()));
    connect(libraryMngrComp.getNegative(DEndpointCtrlPort.class), storageMngrComp.getPositive(DEndpointCtrlPort.class),
      Channel.TWO_WAY);
    connect(libraryMngrComp.getNegative(TorrentMngrPort.class), torrentMngrComp.getPositive(TorrentMngrPort.class),
      Channel.TWO_WAY);
    connect(libraryMngrComp.getNegative(TransferCtrlPort.class), torrentMngrComp.getPositive(TransferCtrlPort.class),
      Channel.TWO_WAY);
    connect(libraryMngrComp.getNegative(TorrentStatusPort.class), torrentMngrComp.getPositive(TorrentStatusPort.class),
      Channel.TWO_WAY);
  }

  private void setSystemSyncI() {
    List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
    resp.add(SystemAddressEvent.Response.class);
    systemSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(SystemPort.class, resp));
    init.systemSyncIComp.setComp(systemSyncIComp);

    connect(systemSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
    connect(systemSyncIComp.getNegative(SystemPort.class), libraryMngrComp.getPositive(SystemPort.class),
      Channel.TWO_WAY);
  }

  private void setTorrentSyncI() {
    List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
    resp.add(HopsTorrentDownloadEvent.StartSuccess.class);
    resp.add(HopsTorrentDownloadEvent.StartFailed.class);
    resp.add(HopsTorrentDownloadEvent.AdvanceResponse.class);
    resp.add(HopsTorrentUploadEvent.Success.class);
    resp.add(HopsTorrentUploadEvent.Failed.class);
    resp.add(HopsTorrentStopEvent.Response.class);
    resp.add(HopsContentsEvent.Response.class);
    resp.add(TorrentExtendedStatusEvent.Response.class);

    hopsTorrentSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(HopsTorrentPort.class, resp));
    init.hopsTorrentSyncIComp.setComp(hopsTorrentSyncIComp);

    connect(hopsTorrentSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
    connect(hopsTorrentSyncIComp.getNegative(HopsTorrentPort.class), libraryMngrComp.getPositive(HopsTorrentPort.class),
      Channel.TWO_WAY);
  }

  private static OverlayIdFactory setupOverlayIdFactory() {
    OverlayRegistry.initiate(new SystemOverlays.TypeFactory(), new SystemOverlays.Comparator());

    byte torrentOwnerId = 1;
    OverlayRegistry.registerPrefix(TorrentIds.TORRENT_OVERLAYS, torrentOwnerId);

    IdentifierFactory torrentBaseIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.OVERLAY.toString());
    return new OverlayIdFactory(torrentBaseIdFactory, TorrentIds.Types.TORRENT, torrentOwnerId);
  }

  private static void setupSerializers() {
    int serializerId = 128;
    serializerId = BasicSerializerSetup.registerBasicSerializers(serializerId);
    serializerId = CroupierSerializerSetup.registerSerializers(serializerId);
    serializerId = GradientSerializerSetup.registerSerializers(serializerId);
    serializerId = OMngrSerializerSetup.registerSerializers(serializerId);
    serializerId = NetworkMngrSerializerSetup.registerSerializers(serializerId);
    serializerId = StunSerializerSetup.registerSerializers(serializerId);
    serializerId = GVoDSerializerSetup.registerSerializers(serializerId);
    serializerId = LedbatSerializerSetup.registerSerializers(serializerId);
  }

  private static void setupFSM(Config.Builder builder) throws FSMException {
    FSMIdentifierFactory fsmIdFactory = FSMIdentifierFactory.DEFAULT;
    fsmIdFactory.registerFSMDefId(LibTFSM.NAME);
    builder.setValue(FSMIdentifierFactory.CONFIG_KEY, fsmIdFactory);
  }

  private static String getDelaBaseDir() throws URISyntaxException {
    String jarPath = VoDNatLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    String delaDir = jarPath;
    //remove jar name
    delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
    //remove bin dir
    delaDir = delaDir.substring(0, delaDir.lastIndexOf(File.separator));
    return delaDir;
  }

  private static void setupPaths(Config.Builder builder) throws URISyntaxException {
    String delaBaseDir = getDelaBaseDir();
    String webServerConfig = delaBaseDir + File.separator + "conf" + File.separator + "config.yml";
    builder.setValue("system.dir", delaBaseDir);
    builder.setValue("webservice.server", webServerConfig);
    Optional<String> librarySummary = builder.readValue("hops.library.type");
    if (librarySummary.isPresent() && librarySummary.get().toLowerCase().equals("disk")) {
      String librarySummaryPath = delaBaseDir + File.separator + "library.summary";
      builder.setValue("hops.library.disk.summary", librarySummaryPath);
    }
  }

  private static void setupBasic(Config.Builder builder) {
    Random rand = new Random();
    Long seed = builder.getValue("system.seed", Long.class);
    if (seed == null) {
      builder.setValue("system.seed", rand.nextLong());
    }
  }

  private static OverlayIdFactory setupSystem() throws FSMException, URISyntaxException {
    Config.Impl config = (Config.Impl) Kompics.getConfig();
    Config.Builder builder = Kompics.getConfig().modify(UUID.randomUUID());
    setupBasic(builder);
    setupPaths(builder);
    setupFSM(builder);
    TorrentIds.registerDefaults(builder.getValue("system.seed", Long.class));
    OverlayIdFactory torrentIdFactory = setupOverlayIdFactory();
    setupSerializers();
    config.apply(builder.finalise(), (Optional) Optional.absent());
    Kompics.setConfig(config);
    return torrentIdFactory;
  }

  private static class FutureComponent implements DozySyncI {

    public final SettableFuture<DozySyncI> futureComp = SettableFuture.create();

    @Override
    public boolean isReady() {
      return futureComp.isDone();
    }

    @Override
    public <E extends KompicsEvent & Identifiable> DozyResult sendReq(E req, long timeout) {
      try {
        return futureComp.get().sendReq(req, timeout);
      } catch (InterruptedException | ExecutionException ex) {
        throw new RuntimeException(ex);
      }
    }

    public void setComp(Component comp) {
      futureComp.set((DozySyncI) comp.getComponent());
    }
  }

  private static Pair<DropwizardDela, String[]> webServer(Init init, OverlayIdFactory torrentIdFactory) {
    Map<String, DozySyncI> synchronousInterfaces = new HashMap<>();
    synchronousInterfaces.put(DozyVoD.systemDozyName, init.systemSyncIComp);
    synchronousInterfaces.put(DozyVoD.hopsTorrentDozyName, init.hopsTorrentSyncIComp);

    List<DozyResource> resources = new ArrayList<>();
    resources.add(new VoDEndpointREST());

    resources.add(new HTStartDownloadREST.Basic(torrentIdFactory));
    resources.add(new HTStartDownloadREST.XML(torrentIdFactory));
    resources.add(new HTAdvanceDownloadREST.Basic(torrentIdFactory));
    resources.add(new HTAdvanceDownloadREST.XML(torrentIdFactory));
    resources.add(new HTUploadREST.Basic(torrentIdFactory));
    resources.add(new HTUploadREST.XML(torrentIdFactory));
    resources.add(new HTStopREST(torrentIdFactory));
    resources.add(new HTContentsREST.Basic(torrentIdFactory));
    resources.add(new HTContentsREST.Hops(torrentIdFactory));
    resources.add(new TorrentExtendedStatusREST(torrentIdFactory));

    String delaBaseDir = Kompics.getConfig().getValue("system.dir", String.class);
    DropwizardDela webserver = new DropwizardDela(synchronousInterfaces, resources, delaBaseDir);

    String webserviceConfig = Kompics.getConfig().getValue("webservice.server", String.class);
    LOG.info("webservices config:{}", webserviceConfig);
    String[] args = new String[]{"server", webserviceConfig};

    return Pair.with(webserver, args);
  }

  public static void main(String[] args) throws IOException, FSMException, URISyntaxException {
    OverlayIdFactory torrentIdFactory = setupSystem();
    Init init = new Init();
    Pair<DropwizardDela, String[]> webserver = webServer(init, torrentIdFactory);
    
    Thread thread = new Thread(() -> {
      try {
        webserver.getValue0().run(webserver.getValue1());
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
    thread.start();
    
    if (Kompics.isOn()) {
      Kompics.shutdown();
    }
    // Yes 20 is totally arbitrary
    Kompics.createAndStart(VoDNatLauncher.class, init, Runtime.getRuntime().availableProcessors(), 20);
    try {
      Kompics.waitForTermination();
    } catch (InterruptedException ex) {
      System.exit(1);
    }
  }

  private static class Init extends se.sics.kompics.Init<VoDNatLauncher> {

    public final FutureComponent systemSyncIComp = new FutureComponent();
    public final FutureComponent hopsTorrentSyncIComp = new FutureComponent();
  }
}
