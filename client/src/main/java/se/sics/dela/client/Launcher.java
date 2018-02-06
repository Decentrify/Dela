package se.sics.dela.client;

import se.sics.kompics.ComponentDefinition;

public class Launcher extends ComponentDefinition {

//  private Logger LOG = LoggerFactory.getLogger(Launcher.class);
//  private String logPrefix = "";
//
//  //*****************************CONNECTIONS**********************************
//  //********************INTERNAL_DO_NOT_CONNECT_TO****************************
//  private Positive<StatusPort> otherStatusPort = requires(StatusPort.class);
//  private Positive<HopsTorrentPort> torrentPort = requires(HopsTorrentPort.class);
//  private Positive<Timer> timerPort = requires(Timer.class);
//  //****************************EXTERNAL_STATE********************************
//  private KAddress selfAdr;
//  //****************************INTERNAL_STATE********************************
//  private Component timerComp;
//  private Component networkMngrComp;
//  private Component libraryMngrComp;
//  private Component torrentMngrComp;
//  private Component storageMngrComp;
//  //**************************************************************************
//  private Download.State downloadState;
//  private WebTarget hopssite;
//
//  public Launcher() {
//    LOG.debug("{}starting...", logPrefix);
//
//    downloadState = new Download.State();
//    subscribe(handleStart, control);
//    subscribe(handleNetReady, otherStatusPort);
//    subscribe(Download.getHandleDownloadStatus(downloadState), timerPort);
//    subscribe(Download.getHandleDownloadStatusResponse(downloadState), torrentPort);
//    subscribe(Download.handleTorrentResult, torrentPort);
//
//    systemSetup();
//    hopssite = getHopssite();
//    downloadState.setHopssite(hopssite);
//    downloadState.setLibDir(System.getProperty("user.dir"));
//    downloadState.setDownloadStatusPeriod(1000); //1s
//  }
//
//  private WebTarget getHopssite() {
//    String hopssiteVersion = config().getValue("hopssite.version", String.class);
//    String hopssiteTarget = config().getValue("hopssite.target", String.class);
//    return WebClient.getClient(hopssiteVersion, hopssiteTarget);
//  }
//
//  private void systemSetup() {
//    //dela dir
//    String delaS = config().getValue("hops.library.disk.summary", String.class);
//    if (delaS == null) {
//      delaS = System.getProperty("user.home") + File.separator + ".dela" + File.separator + "library.summary";
//      Config.Builder cb = config().modify(id());
//      cb.setValue("hops.library.disk.summary", delaS);
//      ConfigUpdate cu = cb.finalise();
//      updateConfig(cu);
//    }
//    File dela = new File(delaS);
//    if (!dela.exists()) {
//      dela.getParentFile().mkdirs();
//    }
//    //ipv4
//    System.setProperty("java.net.preferIPv4Stack", "true");
//    //config
//    Long seed = config().getValue("system.seed", Long.class);
//    if (seed == null) {
//      Random rand = new Random();
//      seed = rand.nextLong();
//      Config.Builder cb = config().modify(id());
//      cb.setValue("system.seed", seed);
//      ConfigUpdate cu = cb.finalise();
//      updateConfig(cu);
//    }
//    //identifier setup
//    TorrentIds.registerDefaults(seed);
//
//    overlaysSetup();
//    serializersSetup();
//  }
//
//  private void overlaysSetup() {
//    OverlayRegistry.initiate(new SystemOverlays.TypeFactory(), new SystemOverlays.Comparator());
//
//    byte torrentOwnerId = 1;
//    OverlayRegistry.registerPrefix(TorrentIds.TORRENT_OVERLAYS, torrentOwnerId);
//
//    IdentifierFactory torrentBaseIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.OVERLAY.toString());
//    OverlayIdFactory torrentIdFactory
//      = new OverlayIdFactory(torrentBaseIdFactory, TorrentIds.Types.TORRENT, torrentOwnerId);
//    downloadState.setTorrentIdFactory(torrentIdFactory);
//  }
//
//  private void serializersSetup() {
//    MessageRegistrator.register();
//    int serializerId = 128;
//    serializerId = BasicSerializerSetup.registerBasicSerializers(serializerId);
//    serializerId = CroupierSerializerSetup.registerSerializers(serializerId);
//    serializerId = GradientSerializerSetup.registerSerializers(serializerId);
//    serializerId = OMngrSerializerSetup.registerSerializers(serializerId);
//    serializerId = NetworkMngrSerializerSetup.registerSerializers(serializerId);
//    serializerId = StunSerializerSetup.registerSerializers(serializerId);
//    serializerId = GVoDSerializerSetup.registerSerializers(serializerId);
//    serializerId = LedbatSerializerSetup.registerSerializers(serializerId);
//  }
//
//  Handler handleStart = new Handler<Start>() {
//    @Override
//    public void handle(Start event) {
//      LOG.info("{}starting", logPrefix);
//
//      String delaVersion = Hopssite.getDelaVersion(hopssite);
//      LOG.info("dela version:{}", delaVersion);
//      if (!delaVersion.equals("0.1")) {
//        LOG.error("wrong dela version");
//        Kompics.shutdown();
//      }
//      timerComp = create(JavaTimer.class, Init.NONE);
//      connect(timerPort.getPair(), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//      setNetworkMngr();
//
//      trigger(Start.event, timerComp.control());
//      trigger(Start.event, networkMngrComp.control());
//    }
//  };
//
//  private void setNetworkMngr() {
//    LOG.info("{}setting up network mngr", logPrefix);
//    SimpleNatMngrComp.ExtPort netExtPorts = new SimpleNatMngrComp.ExtPort(timerComp.getPositive(Timer.class));
//    networkMngrComp = create(SimpleNatMngrComp.class, new SimpleNatMngrComp.Init(netExtPorts));
//    connect(networkMngrComp.getPositive(StatusPort.class), otherStatusPort.getPair(), Channel.TWO_WAY);
//  }
//
//  ClassMatchedHandler handleNetReady
//    = new ClassMatchedHandler<NetMngrReady, Status.Internal<NetMngrReady>>() {
//      @Override
//      public void handle(NetMngrReady content, Status.Internal<NetMngrReady> container) {
//        LOG.info("{}setting up dela with adr:{}", new Object[]{logPrefix, content.systemAdr});
//        selfAdr = content.systemAdr;
//
//        setStorageMngr();
//        setTorrentMngr();
//        setLibraryMngr();
//
//        trigger(Start.event, storageMngrComp.control());
//        trigger(Start.event, torrentMngrComp.control());
//        trigger(Start.event, libraryMngrComp.control());
//
//        LOG.info("{}dela started", logPrefix);
//        downloadState = downloadState.setConnection(proxy, torrentPort, timerPort);
////        readCommand();
//        doCommand();
//      }
//    };
//
//  private void setStorageMngr() {
//    storageMngrComp = create(DStorageMngrComp.class, new DStorageMngrComp.Init(selfAdr.getId()));
//  }
//
//  private void setTorrentMngr() {
//    torrentMngrComp = create(TorrentMngrComp.class, new TorrentMngrComp.Init(selfAdr));
//    connect(torrentMngrComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//    connect(torrentMngrComp.getNegative(Network.class), networkMngrComp.getPositive(Network.class), Channel.TWO_WAY);
//    connect(torrentMngrComp.getNegative(DStreamControlPort.class), storageMngrComp.getPositive(DStreamControlPort.class),
//      Channel.TWO_WAY);
//    connect(torrentMngrComp.getNegative(DStoragePort.class), storageMngrComp.getPositive(DStoragePort.class),
//      Channel.TWO_WAY);
//  }
//
//  private void setLibraryMngr() {
//    CoreExtPorts extPorts = new CoreExtPorts(timerComp.getPositive(Timer.class), networkMngrComp.getPositive(
//      Network.class));
//    libraryMngrComp = create(LibraryMngrComp.class, new LibraryMngrComp.Init(selfAdr, new HopsLibraryProvider()));
//    connect(libraryMngrComp.getNegative(DEndpointCtrlPort.class), storageMngrComp.getPositive(DEndpointCtrlPort.class),
//      Channel.TWO_WAY);
//    connect(libraryMngrComp.getNegative(TorrentMngrPort.class), torrentMngrComp.getPositive(TorrentMngrPort.class),
//      Channel.TWO_WAY);
//    connect(libraryMngrComp.getNegative(TransferCtrlPort.class), torrentMngrComp.getPositive(TransferCtrlPort.class),
//      Channel.TWO_WAY);
//    connect(libraryMngrComp.getNegative(TorrentStatusPort.class), torrentMngrComp.getPositive(TorrentStatusPort.class),
//      Channel.TWO_WAY);
//    connect(torrentPort.getPair(), libraryMngrComp.getPositive(HopsTorrentPort.class), Channel.TWO_WAY);
//  }
//
//  private static void setupFSM() throws FSMException {
//    FSMIdentifierFactory fsmIdFactory = FSMIdentifierFactory.DEFAULT;
//    fsmIdFactory.registerFSMDefId(LibTFSM.NAME);
//
//    Config.Impl config = (Config.Impl) Kompics.getConfig();
//    Config.Builder builder = Kompics.getConfig().modify(UUID.randomUUID());
//    builder.setValue(FSMIdentifierFactory.CONFIG_KEY, fsmIdFactory);
//    config.apply(builder.finalise(), (Optional) Optional.absent());
//    Kompics.setConfig(config);
//  }
//
//  private void readCommand() {
//    LOG.info("commands:");
//    LOG.info("1) download:dataset - example: download:datasetA_1");
//    LOG.info("waiting for command...");
//    Scanner scanner = new Scanner(System.in);
//    String input = scanner.nextLine();
//    LOG.info("command:{}", input);
//    if (input.startsWith("download")) {
//      String dataset = input.substring(input.indexOf(":") + 1);
//      downloadState = downloadState.download(dataset);
//      downloadState = Download.setupDownload.apply(downloadState);
//    }
//  }
//
//  private void doCommand() {
//    String dataset = "datasetA_1";
//    downloadState = downloadState.download(dataset);
//    downloadState = Download.setupDownload.apply(downloadState);
//  }
//
//  public static void main(String[] args) throws IOException, FSMException {
//    if (Kompics.isOn()) {
//      Kompics.shutdown();
//    }
//    setupFSM();
//    Kompics.createAndStart(Launcher.class, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
//    try {
//      Kompics.waitForTermination();
//    } catch (InterruptedException ex) {
//      System.exit(1);
//    }
//  }
}
