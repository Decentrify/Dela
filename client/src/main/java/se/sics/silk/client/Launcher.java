package se.sics.silk.client;

import com.google.common.base.Optional;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.caracaldb.MessageRegistrator;
import se.sics.gvod.network.GVoDSerializerSetup;
import se.sics.kompics.Channel;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
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
import se.sics.nstream.storage.durable.DStorageMngrComp;
import se.sics.nstream.storage.durable.DStoragePort;
import se.sics.nstream.storage.durable.DStreamControlPort;
import se.sics.silk.SystemOverlays;
import se.sics.silk.r2torrent.R2TorrentComp;
import se.sics.silk.r2torrent.torrent.R1FileDownload;
import se.sics.silk.r2torrent.torrent.R1FileUpload;
import se.sics.silk.r2torrent.torrent.R1Torrent;
import se.sics.silk.r2torrent.transfer.R1TransferLeecher;
import se.sics.silk.r2torrent.transfer.R1TransferSeeder;
import se.sics.silkold.torrentmngr.TorrentMngrFSM;

public class Launcher extends ComponentDefinition {

  private Logger LOG = LoggerFactory.getLogger(Launcher.class);
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

    clientSetup();
    systemSetup();
  }

  private void clientSetup() {
    //dela dir
    String delaS = config().getValue("hops.library.disk.summary", String.class);
    if (delaS == null) {
      delaS = System.getProperty("user.home") + File.separator + ".dela" + File.separator + "library.summary";
      Config.Builder cb = config().modify(id());
      cb.setValue("hops.library.disk.summary", delaS);
      ConfigUpdate cu = cb.finalise();
      updateConfig(cu);
    }
    File dela = new File(delaS);
    if (!dela.exists()) {
      dela.getParentFile().mkdirs();
    }
    //ipv4
    System.setProperty("java.net.preferIPv4Stack", "true");
    //config
    Long seed = config().getValue("system.seed", Long.class);
    if (seed == null) {
      Random rand = new Random();
      seed = rand.nextLong();
      Config.Builder cb = config().modify(id());
      cb.setValue("system.seed", seed);
      ConfigUpdate cu = cb.finalise();
      updateConfig(cu);
    }
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
        doCommand();
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

//  private void readCommand() {
//    LOG.info("commands:");
//    LOG.info("1) download:dataset - example: download:datasetA_1");
//    LOG.info("waiting for command...");
//    Scanner scanner = new Scanner(System.in);
//    String input = scanner.nextLine();
//    LOG.info("command:{}", input);
//    if (input.startsWith("download")) {
//      String dataset = input.substring(input.indexOf(":") + 1);
//    }
//  }

  private void doCommand() {
    String dataset = "datasetA_1";
  }

  public static void main(String[] args) throws IOException, FSMException {
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
}
