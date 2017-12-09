package se.sics.dela.client;

import com.google.common.base.Optional;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.UUID;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
import se.sics.nstream.hops.SystemOverlays;
import se.sics.nstream.hops.libmngr.fsm.LibTFSM;
import se.sics.nstream.hops.library.HopsLibraryProvider;
import se.sics.nstream.library.LibraryMngrComp;
import se.sics.nstream.storage.durable.DEndpointCtrlPort;
import se.sics.nstream.storage.durable.DStorageMngrComp;
import se.sics.nstream.storage.durable.DStoragePort;
import se.sics.nstream.storage.durable.DStreamControlPort;
import se.sics.nstream.torrent.TorrentMngrComp;
import se.sics.nstream.torrent.TorrentMngrPort;
import se.sics.nstream.torrent.tracking.TorrentStatusPort;
import se.sics.nstream.torrent.transfer.TransferCtrlPort;
import se.sics.nstream.util.CoreExtPorts;

public class Launcher extends ComponentDefinition {

  private Logger LOG = LoggerFactory.getLogger(Launcher.class);
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
  //**************************************************************************
  private OverlayIdFactory torrentIdFactory;
  private WebTarget hopssite;

  public Launcher() {
    LOG.debug("{}starting...", logPrefix);

    subscribe(handleStart, control);
    subscribe(handleNetReady, otherStatusPort);

    systemSetup();
    setClient();
  }

  private void systemSetup() {
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
    //identifier setup
    TorrentIds.registerDefaults(seed);

    overlaysSetup();
    serializersSetup();
  }

  private void overlaysSetup() {
    OverlayRegistry.initiate(new SystemOverlays.TypeFactory(), new SystemOverlays.Comparator());

    byte torrentOwnerId = 1;
    OverlayRegistry.registerPrefix(TorrentIds.TORRENT_OVERLAYS, torrentOwnerId);

    IdentifierFactory torrentBaseIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.OVERLAY.toString());
    torrentIdFactory = new OverlayIdFactory(torrentBaseIdFactory, TorrentIds.Types.TORRENT, torrentOwnerId);
  }

  private void serializersSetup() {
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
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting", logPrefix);

      String delaVersion = getDelaVersion();
      LOG.info("dela version:{}", delaVersion);
      if(!delaVersion.equals("0.1")) {
        LOG.error("wrong dela version");
        Kompics.shutdown();
      }
      timerComp = create(JavaTimer.class, Init.NONE);
      setNetworkMngr();

      trigger(Start.event, timerComp.control());
      trigger(Start.event, networkMngrComp.control());
    }
  };

  private void setClient() {

    String hopssiteVersion = config().getValue("hopssite.version", String.class);
    String hopssiteTarget = config().getValue("hopssite.target", String.class);
    TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }
    };
    
    HostnameVerifier anyHost = new HostnameVerifier() {
      @Override
      public boolean verify(String string, SSLSession ssls) {
        return true;
      }
    };

    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      if (hopssiteVersion.equals("bbc5")) {
        hopssite = ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier(anyHost).build().target(hopssiteTarget);
      }
    } catch (NoSuchAlgorithmException | KeyManagementException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String getDelaVersion() {
    WebTarget webTarget = hopssite.path(Hopssite.ClusterService.delaVersion());
    try {
      LOG.info("path:{}", new Object[]{webTarget.getUri().toString()});
      Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
      if (response.getStatus() == Response.Status.OK.getStatusCode()) {
        return response.readEntity(String.class);
      } else {
        throw new RuntimeException("problem contacting hopssite - dela version");
      }
    } catch (ProcessingException ex) {
      throw new RuntimeException("problem contacting hopssite - dela version");
    }
  }

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
        LOG.info("{}setting up dela", logPrefix);
        selfAdr = content.systemAdr;

        setStorageMngr();
        setTorrentMngr();
        setLibraryMngr();

        trigger(Start.event, storageMngrComp.control());
        trigger(Start.event, torrentMngrComp.control());
        trigger(Start.event, libraryMngrComp.control());

        LOG.info("{}dela started", logPrefix);
      }
    };

  private void setStorageMngr() {
    storageMngrComp = create(DStorageMngrComp.class, new DStorageMngrComp.Init(selfAdr.getId()));
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

  private static void setupFSM() throws FSMException {
    FSMIdentifierFactory fsmIdFactory = FSMIdentifierFactory.DEFAULT;
    fsmIdFactory.registerFSMDefId(LibTFSM.NAME);

    Config.Impl config = (Config.Impl) Kompics.getConfig();
    Config.Builder builder = Kompics.getConfig().modify(UUID.randomUUID());
    builder.setValue(FSMIdentifierFactory.CONFIG_KEY, fsmIdFactory);
    config.apply(builder.finalise(), (Optional) Optional.absent());
    Kompics.setConfig(config);
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
