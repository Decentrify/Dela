package se.sics.silk.client;

import com.google.common.base.Optional;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.caracaldb.MessageRegistrator;
import se.sics.gvod.network.GVoDSerializerSetup;
import se.sics.kompics.Channel;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.ComponentProxy;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.config.Config;
import se.sics.kompics.config.ConfigUpdate;
import se.sics.kompics.fsm.FSMException;
import se.sics.kompics.fsm.id.FSMIdentifierFactory;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.kompics.util.Identifier;
import se.sics.ktoolbox.croupier.CroupierSerializerSetup;
import se.sics.ktoolbox.gradient.GradientSerializerSetup;
import se.sics.ktoolbox.netmngr.NetworkMngrSerializerSetup;
import se.sics.ktoolbox.netmngr.event.NetMngrReady;
import se.sics.ktoolbox.omngr.OMngrSerializerSetup;
import se.sics.ktoolbox.util.config.options.BasicAddressBootstrapOption;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.BasicIdentifiers;
import se.sics.ktoolbox.util.identifiable.IdentifierFactory;
import se.sics.ktoolbox.util.identifiable.IdentifierRegistry;
import se.sics.ktoolbox.util.identifiable.basic.IntIdFactory;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayIdFactory;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayRegistry;
import se.sics.ktoolbox.util.managedStore.core.util.HashUtil;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.setup.BasicSerializerSetup;
import se.sics.ktoolbox.util.status.Status;
import se.sics.ktoolbox.util.status.StatusPort;
import se.sics.ledbat.LedbatSerializerSetup;
import se.sics.nat.mngr.SimpleNatMngrComp;
import se.sics.nat.stun.StunSerializerSetup;
import se.sics.nstream.StreamId;
import se.sics.nstream.TorrentIds;
import se.sics.nstream.hops.storage.disk.DiskEndpoint;
import se.sics.nstream.hops.storage.disk.DiskResource;
import se.sics.nstream.storage.durable.DStorageMngrComp;
import se.sics.nstream.storage.durable.DStoragePort;
import se.sics.nstream.storage.durable.DStreamControlPort;
import se.sics.nstream.storage.durable.util.MyStream;
import se.sics.nstream.storage.durable.util.StreamEndpoint;
import se.sics.nstream.storage.durable.util.StreamResource;
import se.sics.nstream.util.BlockDetails;
import se.sics.silk.SystemOverlays;
import se.sics.silk.TorrentIdHelper;
import se.sics.silk.r2torrent.R2TorrentComp;
import se.sics.silk.r2torrent.torrent.R1FileDownload;
import se.sics.silk.r2torrent.torrent.R1FileUpload;
import se.sics.silk.r2torrent.torrent.R1Torrent;
import se.sics.silk.r2torrent.torrent.R1TorrentCtrlPort;
import se.sics.silk.r2torrent.torrent.event.R1TorrentCtrlEvents;
import se.sics.silk.r2torrent.torrent.util.R1FileMetadata;
import se.sics.silk.r2torrent.torrent.util.R1TorrentDetails;
import se.sics.silk.r2torrent.transfer.R1TransferLeecher;
import se.sics.silk.r2torrent.transfer.R1TransferSeeder;
import se.sics.silkold.torrentmngr.TorrentMngrFSM;

public class Launcher extends ComponentDefinition {

  private static Logger LOG = LoggerFactory.getLogger(Launcher.class);
  private String logPrefix = "";

  //*****************************CONNECTIONS**********************************
  //********************INTERNAL_DO_NOT_CONNECT_TO****************************
  private Positive<StatusPort> otherStatusPort = requires(StatusPort.class);
  private Positive<Timer> timerPort = requires(Timer.class);
  //****************************EXTERNAL_STATE********************************
  private KAddress selfAdr;
  //****************************INTERNAL_STATE********************************
  private Component timerComp;
  private Component networkMngrComp;
  private Component storageMngrComp;
  private Component torrentMngrComp;

  public Launcher() {
    LOG.debug("{}starting...", logPrefix);

    subscribe(handleStart, control);
    subscribe(handleNetReady, otherStatusPort);

    clientConfigSetup();
    systemSetup();
  }

  private void clientConfigSetup() {
    //ipv4
    System.setProperty("java.net.preferIPv4Stack", "true");

    //kompics config
    Config.Builder cb = config().modify(id());
    //
    Long seed = config().getValue(ClientConfig.SYSTEM_SEED, Long.class);
    if (seed == null) {
      Random rand = new Random();
      seed = rand.nextLong();
      cb.setValue("system.seed", seed);
    }
    //
    String librarySummaryPath = config().getValue(ClientConfig.LIBRARY_SUMMARY, String.class);
    if (librarySummaryPath == null) {
      librarySummaryPath = System.getProperty("user.home") + File.separator + ".dela" + File.separator
        + "library.summary";
      cb.setValue(ClientConfig.LIBRARY_SUMMARY, librarySummaryPath);
    }
    File librarySummaryFile = new File(librarySummaryPath);
    if (!librarySummaryFile.exists()) {
      librarySummaryFile.getParentFile().mkdirs();
      try {
        if (!librarySummaryFile.createNewFile()) {
          throw new RuntimeException("ups");
        }
      } catch (IOException ex) {
        throw new RuntimeException("ups");
      }
    }
    //
    String libraryPath = config().getValue(ClientConfig.LIBRARY_PATH, String.class);
    if (libraryPath == null) {
      libraryPath = System.getProperty("user.home") + File.separator + ".dela" + File.separator + "library";
      cb.setValue(ClientConfig.LIBRARY_SUMMARY, libraryPath);
    }
    File librarySummaryDir = new File(libraryPath);
    if (!librarySummaryDir.exists()) {
      librarySummaryDir.mkdirs();
    }
    //
    ConfigUpdate cu = cb.finalise();
    updateConfig(cu);
  }

  private void systemSetup() {
    //identifier setup
    TorrentIds.registerDefaults(config().getValue("system.seed", Long.class));

    overlaysSetup();
    serializersSetup();
  }

  private void overlaysSetup() {
    OverlayRegistry.initiate(new SystemOverlays.TypeFactory(), new SystemOverlays.Comparator());

    byte torrentOwnerId = 1;
    OverlayRegistry.registerPrefix(TorrentIds.TORRENT_OVERLAYS, torrentOwnerId);

    IdentifierFactory torrentBaseIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.OVERLAY.toString());
    OverlayIdFactory torrentIdFactory
      = new OverlayIdFactory(torrentBaseIdFactory, TorrentIds.Types.TORRENT, torrentOwnerId);
  }

  private int serializersSetup() {
    MessageRegistrator.register();
    int serializerId = 128;
    serializerId = BasicSerializerSetup.registerBasicSerializers(serializerId);
    serializerId = CroupierSerializerSetup.registerSerializers(serializerId);
    serializerId = GradientSerializerSetup.registerSerializers(serializerId);
    serializerId = OMngrSerializerSetup.registerSerializers(serializerId);
    serializerId = NetworkMngrSerializerSetup.registerSerializers(serializerId);
    serializerId = StunSerializerSetup.registerSerializers(serializerId);
    serializerId = GVoDSerializerSetup.registerSerializers(serializerId);
    serializerId = LedbatSerializerSetup.registerSerializers(serializerId);
    return serializerId;
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting", logPrefix);

      timerComp = create(JavaTimer.class, Init.NONE);
      connect(timerPort.getPair(), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
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
        LOG.info("{}setting up dela with adr:{}", new Object[]{logPrefix, content.systemAdr});
        selfAdr = content.systemAdr;

        setStorageMngr();
        setTorrentMngr();
        
        trigger(Start.event, storageMngrComp.control());
        trigger(Start.event, torrentMngrComp.control());

        LOG.info("{}dela started", logPrefix);
        Driver driver = new Driver(proxy, config(), torrentMngrComp);
        driver.doCommand();
      }
    };

  private void setStorageMngr() {
    storageMngrComp = create(DStorageMngrComp.class, new DStorageMngrComp.Init(selfAdr.getId()));
    connect(storageMngrComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
  }

  private void setTorrentMngr() {
    torrentMngrComp = create(R2TorrentComp.class, new R2TorrentComp.Init(selfAdr));
    connect(torrentMngrComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(Network.class), networkMngrComp.getPositive(Network.class), Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(DStreamControlPort.class), storageMngrComp.getPositive(DStreamControlPort.class),
      Channel.TWO_WAY);
    connect(torrentMngrComp.getNegative(DStoragePort.class), storageMngrComp.getPositive(DStoragePort.class),
      Channel.TWO_WAY);
  }
  
  private static void setupFSM() throws FSMException {
    FSMIdentifierFactory fsmIdFactory = FSMIdentifierFactory.DEFAULT;
    fsmIdFactory.registerFSMDefId(TorrentMngrFSM.NAME);
    fsmIdFactory.registerFSMDefId(R1Torrent.NAME);
    fsmIdFactory.registerFSMDefId(R1FileDownload.NAME);
    fsmIdFactory.registerFSMDefId(R1FileUpload.NAME);
    fsmIdFactory.registerFSMDefId(R1TransferSeeder.NAME);
    fsmIdFactory.registerFSMDefId(R1TransferLeecher.NAME);

    Config.Impl config = (Config.Impl) Kompics.getConfig();
    Config.Builder builder = Kompics.getConfig().modify(UUID.randomUUID());
    builder.setValue(FSMIdentifierFactory.CONFIG_KEY, fsmIdFactory);
    config.apply(builder.finalise(), (Optional) Optional.absent());
    Kompics.setConfig(config);
  }

  public static void main(String[] args) throws IOException, FSMException {
    Launcher.LOG.info("config file:{}", System.getProperties().getProperty("config.file"));
    if (Kompics.isOn()) {
      Kompics.shutdown();
    }
    setupFSM();
    Kompics.createAndStart(Launcher.class, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
    try {
      Kompics.waitForTermination();
    } catch (InterruptedException ex) {
      System.exit(1);
    }
  }

  public static class ClientConfig {

    public static final String SYSTEM_SEED = "system.seed";
    public static final String DELA = "hops";
    public static final String DELA_BOOTSTRAP = DELA + ".bootstrap";
    public static final String LIBRARY_SUMMARY = DELA + ".library.disk.summary";
    public static final String LIBRARY_PATH = DELA + ".library.path";
    public static final String COMMAND = "command";
    
    public final static BasicAddressBootstrapOption delaBootstrap = new BasicAddressBootstrapOption(DELA_BOOTSTRAP);
  }

  public static class Driver {

    final ComponentProxy proxy;
    final Component torrentMngrComp;
    final Config config;
    OverlayId torrentId;
    R1TorrentDetails torrentDetails;

    public Driver(ComponentProxy proxy, Config config, Component torrentMngrComp) {
      this.proxy = proxy;
      this.torrentMngrComp = torrentMngrComp;
      this.config = config;
    }

    private void torrentDetails(String torrentName) {
      
      torrentId = TorrentIds.torrentId(new BasicBuilders.IntBuilder(1));
      torrentDetails = new R1TorrentDetails(HashUtil.getAlgName(HashUtil.SHA));
      IntIdFactory intIdFactory = new IntIdFactory(new Random());

      Identifier file1 = intIdFactory.id(new BasicBuilders.IntBuilder(1));
      Identifier file2 = intIdFactory.id(new BasicBuilders.IntBuilder(2));
      Identifier file3 = intIdFactory.id(new BasicBuilders.IntBuilder(3));
      Identifier file4 = intIdFactory.id(new BasicBuilders.IntBuilder(4));
      Identifier file5 = intIdFactory.id(new BasicBuilders.IntBuilder(5));

      long fileLength = 1000 * 1000;
      int pieceSize = 1024;
      int nrPieces = 10;
      int nrBlocks = 5;
      BlockDetails defaultBlock = new BlockDetails(pieceSize * nrPieces, nrPieces, pieceSize, pieceSize);
      R1FileMetadata fileMetadata = R1FileMetadata.instance(fileLength, defaultBlock);

      torrentDetails.addMetadata(file1, fileMetadata);
      torrentDetails.addMetadata(file2, fileMetadata);
      torrentDetails.addMetadata(file3, fileMetadata);
      torrentDetails.addMetadata(file4, fileMetadata);
      torrentDetails.addMetadata(file5, fileMetadata);
      
      Identifier endpointId = intIdFactory.id(new BasicBuilders.IntBuilder(0));
      StreamId streamId1 = TorrentIdHelper.streamId(endpointId, torrentId, file1);
      StreamId streamId2 = TorrentIdHelper.streamId(endpointId, torrentId, file2);
      StreamId streamId3 = TorrentIdHelper.streamId(endpointId, torrentId, file3);
      StreamId streamId4 = TorrentIdHelper.streamId(endpointId, torrentId, file4);
      StreamId streamId5 = TorrentIdHelper.streamId(endpointId, torrentId, file5);
      
      StreamEndpoint endpoint = new DiskEndpoint();
      String torrentPath = config.getValue("LIBRARY_PATH", String.class) + File.separator + torrentName;
      StreamResource resource1 = new DiskResource(torrentPath, "file1");
      StreamResource resource2 = new DiskResource(torrentPath, "file2");
      StreamResource resource3 = new DiskResource(torrentPath, "file3");
      StreamResource resource4 = new DiskResource(torrentPath, "file4");
      StreamResource resource5 = new DiskResource(torrentPath, "file5");
      
      MyStream stream1 = new MyStream(endpoint, resource1);
      MyStream stream2 = new MyStream(endpoint, resource2);
      MyStream stream3 = new MyStream(endpoint, resource3);
      MyStream stream4 = new MyStream(endpoint, resource4);
      MyStream stream5 = new MyStream(endpoint, resource5);
      
      torrentDetails.addStorage(file1, streamId1, stream1);
      torrentDetails.addStorage(file2, streamId2, stream2);
      torrentDetails.addStorage(file3, streamId3, stream3);
      torrentDetails.addStorage(file4, streamId4, stream4);
      torrentDetails.addStorage(file5, streamId5, stream5);
    }

    public void doCommand() {
      String command = config.getValue(ClientConfig.COMMAND, String.class);
      LOG.info("performing command:{}", command);
      switch (command) {
        case "upload":
          upload();
          break;
        case "download":
          download();
          break;
      }
    }

    public void upload() {
      torrentDetails("upload");
      R1TorrentCtrlEvents.Upload req = new R1TorrentCtrlEvents.Upload(torrentId, torrentDetails);
      proxy.trigger(req, torrentMngrComp.getPositive(R1TorrentCtrlPort.class));
    }

    
    private void download() {
      torrentDetails("download");
      Set<KAddress> bootstrap = new HashSet<>(ClientConfig.delaBootstrap.readValue(config).get());
      R1TorrentCtrlEvents.Download req = new R1TorrentCtrlEvents.Download(torrentId, torrentDetails, bootstrap);
      proxy.trigger(req, torrentMngrComp.getPositive(R1TorrentCtrlPort.class));
    }
  }
}
