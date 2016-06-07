///*
// * Copyright (C) 2016 Swedish Institute of Computer Science (SICS) Copyright (C)
// * 2016 Royal Institute of Technology (KTH)
// *
// * Dozy is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 2
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package se.sics.dozy.vod.system;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import se.sics.caracaldb.MessageRegistrator;
//import se.sics.dozy.DozyResource;
//import se.sics.dozy.DozySyncComp;
//import se.sics.dozy.DozySyncI;
//import se.sics.dozy.dropwizard.DropwizardDozy;
//import se.sics.dozy.vod.DozyVoD;
//import se.sics.dozy.vod.LibraryAddREST;
//import se.sics.dozy.vod.LibraryContentsREST;
//import se.sics.dozy.vod.LibraryElementREST;
//import se.sics.dozy.vod.TorrentDownloadREST;
//import se.sics.dozy.vod.TorrentStopREST;
//import se.sics.dozy.vod.TorrentUploadREST;
//import se.sics.gvod.mngr.LibraryPort;
//import se.sics.gvod.mngr.TorrentPort;
//import se.sics.gvod.mngr.event.LibraryAddEvent;
//import se.sics.gvod.mngr.event.LibraryContentsEvent;
//import se.sics.gvod.mngr.event.LibraryElementEvent;
//import se.sics.gvod.mngr.event.TorrentDownloadEvent;
//import se.sics.gvod.mngr.event.TorrentStopEvent;
//import se.sics.gvod.mngr.event.TorrentUploadEvent;
//import se.sics.gvod.network.GVoDSerializerSetup;
//import se.sics.gvod.system.VoDHostMngrComp;
//import se.sics.kompics.Channel;
//import se.sics.kompics.ClassMatchedHandler;
//import se.sics.kompics.Component;
//import se.sics.kompics.ComponentDefinition;
//import se.sics.kompics.Handler;
//import se.sics.kompics.Init;
//import se.sics.kompics.Kompics;
//import se.sics.kompics.KompicsEvent;
//import se.sics.kompics.Positive;
//import se.sics.kompics.Start;
//import se.sics.kompics.config.ConfigException;
//import se.sics.kompics.network.Network;
//import se.sics.kompics.timer.Timer;
//import se.sics.kompics.timer.java.JavaTimer;
//import se.sics.ktoolbox.cc.bootstrap.CCOperationPort;
//import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort;
//import se.sics.ktoolbox.cc.mngr.CCMngrComp;
//import se.sics.ktoolbox.cc.mngr.event.CCMngrStatus;
//import se.sics.ktoolbox.croupier.CroupierPort;
//import se.sics.ktoolbox.croupier.CroupierSerializerSetup;
//import se.sics.ktoolbox.croupier.aggregation.CroupierAggregation;
//import se.sics.ktoolbox.gradient.GradientSerializerSetup;
//import se.sics.ktoolbox.gradient.aggregation.GradientAggregation;
//import se.sics.ktoolbox.netmngr.NetworkMngrComp;
//import se.sics.ktoolbox.netmngr.NetworkMngrSerializerSetup;
//import se.sics.ktoolbox.netmngr.event.NetMngrReady;
//import se.sics.ktoolbox.overlaymngr.OMngrSerializerSetup;
//import se.sics.ktoolbox.overlaymngr.OverlayMngrComp;
//import se.sics.ktoolbox.overlaymngr.OverlayMngrPort;
//import se.sics.ktoolbox.util.aggregation.BasicAggregation;
//import se.sics.ktoolbox.util.network.KAddress;
//import se.sics.ktoolbox.util.network.nat.NatAwareAddress;
//import se.sics.ktoolbox.util.overlays.id.OverlayIdRegistry;
//import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort;
//import se.sics.ktoolbox.util.setup.BasicSerializerSetup;
//import se.sics.ktoolbox.util.status.Status;
//import se.sics.ktoolbox.util.status.StatusPort;
//
///**
// * @author Alex Ormenisan <aaor@kth.se>
// */
//public class VoDLauncher extends ComponentDefinition {
//
//    private Logger LOG = LoggerFactory.getLogger(VoDLauncher.class);
//    private String logPrefix = "";
//
//    //*****************************CONNECTIONS**********************************
//    //********************INTERNAL_DO_NOT_CONNECT_TO****************************
//    private Positive<StatusPort> otherStatusPort = requires(StatusPort.class);
//    //****************************EXTERNAL_STATE********************************
//    private KAddress selfAdr;
//    private byte[] schemaId = null;
//    //****************************INTERNAL_STATE********************************
//    private Component timerComp;
//    private Component networkMngrComp;
//    private Component ccMngrComp;
//    private Component overlayMngrComp;
//    private Component vodComp;
//    private Component librarySyncIComp;
//    private Component torrentSyncIComp;
//    private DropwizardDozy webserver;
//
//    public VoDLauncher() {
//        LOG.info("{}starting...", logPrefix);
//
//        subscribe(handleStart, control);
//        subscribe(handleNetReady, otherStatusPort);
//        subscribe(handleCCReady, otherStatusPort);
//        subscribe(handleCaracalDisconnect, otherStatusPort);
//
//        registerSerializers();
//        registerPortTracking();
//    }
//
//    private void registerSerializers() {
//        MessageRegistrator.register();
//        int currentId = 128;
//        currentId = BasicSerializerSetup.registerBasicSerializers(currentId);
//        currentId = CroupierSerializerSetup.registerSerializers(currentId);
//        currentId = GradientSerializerSetup.registerSerializers(currentId);
//        currentId = OMngrSerializerSetup.registerSerializers(currentId);
//        currentId = NetworkMngrSerializerSetup.registerSerializers(currentId);
//        currentId = GVoDSerializerSetup.registerSerializers(currentId);
//    }
//
//    private void registerPortTracking() {
//        BasicAggregation.registerPorts();
//        CroupierAggregation.registerPorts();
//        GradientAggregation.registerPorts();
//    }
//
//    Handler handleStart = new Handler<Start>() {
//        @Override
//        public void handle(Start event) {
//            LOG.info("{}starting", logPrefix);
//
//            timerComp = create(JavaTimer.class, Init.NONE);
//            setNetworkMngr();
//
//            trigger(Start.event, timerComp.control());
//            trigger(Start.event, networkMngrComp.control());
//        }
//    };
//
//    private void setNetworkMngr() {
//        LOG.info("{}setting up network mngr", logPrefix);
//        NetworkMngrComp.ExtPort netExtPorts = new NetworkMngrComp.ExtPort(timerComp.getPositive(Timer.class));
//        networkMngrComp = create(NetworkMngrComp.class, new NetworkMngrComp.Init(netExtPorts));
//        connect(networkMngrComp.getPositive(StatusPort.class), otherStatusPort.getPair(), Channel.TWO_WAY);
//    }
//
//    ClassMatchedHandler handleNetReady
//            = new ClassMatchedHandler<NetMngrReady, Status.Internal<NetMngrReady>>() {
//                @Override
//                public void handle(NetMngrReady content, Status.Internal<NetMngrReady> container) {
//                    LOG.info("{}network mngr ready", logPrefix);
//                    selfAdr = content.systemAdr;
//                    setCCMngr();
//                    trigger(Start.event, ccMngrComp.control());
//                }
//            };
//
//    private void setCCMngr() {
//        LOG.info("{}setting up caracal client", logPrefix);
//        CCMngrComp.ExtPort ccMngrExtPorts = new CCMngrComp.ExtPort(timerComp.getPositive(Timer.class),
//                networkMngrComp.getPositive(Network.class));
//        ccMngrComp = create(CCMngrComp.class, new CCMngrComp.Init(selfAdr, ccMngrExtPorts));
//        connect(ccMngrComp.getPositive(StatusPort.class), otherStatusPort.getPair(), Channel.TWO_WAY);
//    }
//
//    ClassMatchedHandler handleCCReady
//            = new ClassMatchedHandler<CCMngrStatus.Ready, Status.Internal<CCMngrStatus.Ready>>() {
//                @Override
//                public void handle(CCMngrStatus.Ready content, Status.Internal<CCMngrStatus.Ready> container) {
//                    LOG.info("{}caracal client ready", logPrefix);
//                    schemaId = content.schemas.getId("gvod.metadata");
//                    if (schemaId == null) {
//                        LOG.error("exception:vod schema undefined shutting down");
//                        System.exit(1);
//                    }
//
//                    setOverlayMngr();
//                    setVoD();
//                    setLibrarySyncI();
//                    setTorrentSyncI();
//                    setWebserver();
//
//                    trigger(Start.event, overlayMngrComp.control());
//                    trigger(Start.event, vodComp.control());
//                    trigger(Start.event, librarySyncIComp.control());
//                    trigger(Start.event, torrentSyncIComp.control());
//
//                    LOG.info("{}overlay owners:\n{}", logPrefix, OverlayIdRegistry.print());
//                    startWebserver();
//                    LOG.info("{}starting complete...", logPrefix);
//                }
//            };
//
//    private void setOverlayMngr() {
//        LOG.info("{}setting up overlay mngr", logPrefix);
//        OverlayMngrComp.ExtPort oMngrExtPorts = new OverlayMngrComp.ExtPort(timerComp.getPositive(Timer.class),
//                networkMngrComp.getPositive(Network.class), ccMngrComp.getPositive(CCHeartbeatPort.class));
//        overlayMngrComp = create(OverlayMngrComp.class, new OverlayMngrComp.Init((NatAwareAddress) selfAdr, oMngrExtPorts));
//    }
//
//    ClassMatchedHandler handleCaracalDisconnect
//            = new ClassMatchedHandler<CCMngrStatus.Disconnected, Status.Internal<CCMngrStatus.Disconnected>>() {
//
//                @Override
//                public void handle(CCMngrStatus.Disconnected content, Status.Internal<CCMngrStatus.Disconnected> container) {
//                    LOG.debug("Caracal client disconnected, need to initiate counter measures.");
//                }
//            };
//
//    private void setVoD() {
//        VoDHostMngrComp.ExtPort extPorts = new VoDHostMngrComp.ExtPort(
//                timerComp.getPositive(Timer.class), networkMngrComp.getPositive(Network.class), 
//                ccMngrComp.getPositive(CCOperationPort.class), overlayMngrComp.getPositive(OverlayMngrPort.class),
//                overlayMngrComp.getPositive(CroupierPort.class), overlayMngrComp.getNegative(OverlayViewUpdatePort.class));
//        vodComp = create(VoDHostMngrComp.class, new VoDHostMngrComp.Init(selfAdr, extPorts));
//    }
//
//    private void setLibrarySyncI() {
//        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
//        resp.add(LibraryContentsEvent.Response.class);
//        resp.add(LibraryElementEvent.Response.class);
//        resp.add(LibraryAddEvent.Response.class);
//        librarySyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(LibraryPort.class, resp));
//
//        connect(librarySyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//        connect(librarySyncIComp.getNegative(LibraryPort.class), vodComp.getPositive(LibraryPort.class), Channel.TWO_WAY);
//    }
//
//    private void setTorrentSyncI() {
//        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
//        resp.add(TorrentDownloadEvent.Response.class);
//        resp.add(TorrentUploadEvent.Response.class);
//        resp.add(TorrentStopEvent.Response.class);
//        torrentSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(TorrentPort.class, resp));
//        connect(torrentSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//        connect(torrentSyncIComp.getNegative(TorrentPort.class), vodComp.getPositive(TorrentPort.class), Channel.TWO_WAY);
//    }
//
//    private void setWebserver() {
//        Map<String, DozySyncI> synchronousInterfaces = new HashMap<>();
//        synchronousInterfaces.put(DozyVoD.libraryDozyName, (DozySyncI) librarySyncIComp.getComponent());
//        synchronousInterfaces.put(DozyVoD.torrentDozyName, (DozySyncI) torrentSyncIComp.getComponent());
//
//        List<DozyResource> resources = new ArrayList<>();
//        resources.add(new LibraryContentsREST());
//        resources.add(new LibraryElementREST());
//        resources.add(new LibraryAddREST());
//        resources.add(new TorrentDownloadREST());
//        resources.add(new TorrentUploadREST());
//        resources.add(new TorrentStopREST());
//
//        webserver = new DropwizardDozy(synchronousInterfaces, resources);
//    }
//
//    private void startWebserver() {
//        String[] args = new String[]{"server", config().getValue("webservice.server", String.class)};
//        try {
//            webserver.run(args);
//        } catch (ConfigException ex) {
//            LOG.error("{}configuration error:{}", logPrefix, ex.getMessage());
//            throw new RuntimeException(ex);
//        } catch (Exception ex) {
//            LOG.error("{}dropwizard error:{}", logPrefix, ex.getMessage());
//            throw new RuntimeException(ex);
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        if (Kompics.isOn()) {
//            Kompics.shutdown();
//        }
//        Kompics.createAndStart(VoDLauncher.class, Runtime.getRuntime().availableProcessors(), 20); // Yes 20 is totally arbitrary
//        try {
//            Kompics.waitForTermination();
//        } catch (InterruptedException ex) {
//            System.exit(1);
//        }
//    }
//}
