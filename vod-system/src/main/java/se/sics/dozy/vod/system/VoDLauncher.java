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
//import se.sics.dozy.vod.ContentsSummaryREST;
//import se.sics.dozy.vod.HopsConnectionREST;
//import se.sics.dozy.vod.HopsFileDeleteREST;
//import se.sics.dozy.vod.HopsTorrentDownloadREST;
//import se.sics.dozy.vod.HopsTorrentStopREST;
//import se.sics.dozy.vod.HopsTorrentUploadREST;
//import se.sics.dozy.vod.TorrentExtendedStatusREST;
//import se.sics.dozy.vod.VoDEndpointREST;
//import se.sics.gvod.mngr.LibraryPort;
//import se.sics.gvod.mngr.SystemPort;
//import se.sics.gvod.mngr.TorrentPort;
//import se.sics.gvod.mngr.VoDMngrComp;
//import se.sics.gvod.mngr.event.ContentsSummaryEvent;
//import se.sics.gvod.mngr.event.library.HopsFileDeleteEvent;
//import se.sics.gvod.mngr.event.HopsTorrentDownloadEvent;
//import se.sics.gvod.mngr.event.HopsTorrentStopEvent;
//import se.sics.gvod.mngr.event.HopsTorrentUploadEvent;
//import se.sics.gvod.mngr.event.TorrentExtendedStatusEvent;
//import se.sics.gvod.mngr.event.library.HopsFileCreateEvent;
//import se.sics.gvod.mngr.event.system.HopsConnectionEvent;
//import se.sics.gvod.mngr.event.system.SystemAddressEvent;
//import se.sics.gvod.network.GVoDSerializerSetup;
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
//import se.sics.ktoolbox.croupier.CroupierSerializerSetup;
//import se.sics.ktoolbox.gradient.GradientSerializerSetup;
//import se.sics.ktoolbox.netmngr.NetworkMngrComp;
//import se.sics.ktoolbox.netmngr.NetworkMngrSerializerSetup;
//import se.sics.ktoolbox.netmngr.event.NetMngrReady;
//import se.sics.ktoolbox.overlaymngr.OMngrSerializerSetup;
//import se.sics.ktoolbox.util.network.KAddress;
//import se.sics.ktoolbox.util.setup.BasicSerializerSetup;
//import se.sics.ktoolbox.util.status.Status;
//import se.sics.ktoolbox.util.status.StatusPort;
//import se.sics.nat.stun.StunSerializerSetup;
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
//    //****************************INTERNAL_STATE********************************
//    private Component timerComp;
//    private Component networkMngrComp;
//    private Component vodMngrComp;
//    private Component systemSyncIComp;
//    private Component librarySyncIComp;
//    private Component torrentSyncIComp;
//    private DropwizardDozy webserver;
//
//    public VoDLauncher() {
//        LOG.info("{}starting...", logPrefix);
//
//        subscribe(handleStart, control);
//        subscribe(handleNetReady, otherStatusPort);
//
//        registerSerializers();
//        registerPortTracking();
//    }
//
//    private void registerSerializers() {
//        MessageRegistrator.register();
//        int serializerId = 128;
//        serializerId = BasicSerializerSetup.registerBasicSerializers(serializerId);
//        serializerId = CroupierSerializerSetup.registerSerializers(serializerId);
//        serializerId = GradientSerializerSetup.registerSerializers(serializerId);
//        serializerId = OMngrSerializerSetup.registerSerializers(serializerId);
//        serializerId = NetworkMngrSerializerSetup.registerSerializers(serializerId);
//        serializerId = StunSerializerSetup.registerSerializers(serializerId);
//        serializerId = GVoDSerializerSetup.registerSerializers(serializerId);
//    }
//
//    private void registerPortTracking() {
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
//
//                    setVoDMngr();
//                    setSystemSyncI();
//                    setLibrarySyncI();
//                    setTorrentSyncI();
//                    setWebserver();
//
//                    trigger(Start.event, vodMngrComp.control());
//                    trigger(Start.event, librarySyncIComp.control());
//                    trigger(Start.event, torrentSyncIComp.control());
//
//                    startWebserver();
//                    LOG.info("{}starting complete...", logPrefix);
//                }
//            };
//
//    private void setVoDMngr() {
//        VoDMngrComp.ExtPort extPorts = new VoDMngrComp.ExtPort(timerComp.getPositive(Timer.class), networkMngrComp.getPositive(Network.class));
//        vodMngrComp = create(VoDMngrComp.class, new VoDMngrComp.Init(extPorts, selfAdr));
//    }
//
//    private void setSystemSyncI() {
//        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
//        resp.add(SystemAddressEvent.Response.class);
//        resp.add(HopsConnectionEvent.Response.class);
//        systemSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(SystemPort.class, resp));
//
//        connect(systemSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//        connect(systemSyncIComp.getNegative(SystemPort.class), vodMngrComp.getPositive(SystemPort.class), Channel.TWO_WAY);
//    }
//    
//    private void setLibrarySyncI() {
//        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
//        resp.add(ContentsSummaryEvent.Response.class);
//        resp.add(TorrentExtendedStatusEvent.Response.class);
//        resp.add(HopsFileDeleteEvent.Response.class);
//        resp.add(HopsFileCreateEvent.Response.class);
//        librarySyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(LibraryPort.class, resp));
//
//        connect(librarySyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//        connect(librarySyncIComp.getNegative(LibraryPort.class), vodMngrComp.getPositive(LibraryPort.class), Channel.TWO_WAY);
//    }
//
//    private void setTorrentSyncI() {
//        List<Class<? extends KompicsEvent>> resp = new ArrayList<>();
//        resp.add(HopsTorrentDownloadEvent.Response.class);
//        resp.add(HopsTorrentUploadEvent.Response.class);
//        resp.add(HopsTorrentStopEvent.Response.class);
//        torrentSyncIComp = create(DozySyncComp.class, new DozySyncComp.Init(TorrentPort.class, resp));
//        connect(torrentSyncIComp.getNegative(Timer.class), timerComp.getPositive(Timer.class), Channel.TWO_WAY);
//        connect(torrentSyncIComp.getNegative(TorrentPort.class), vodMngrComp.getPositive(TorrentPort.class), Channel.TWO_WAY);
//    }
//
//    private void setWebserver() {
//        Map<String, DozySyncI> synchronousInterfaces = new HashMap<>();
//        synchronousInterfaces.put(DozyVoD.systemDozyName, (DozySyncI) systemSyncIComp.getComponent());
//        synchronousInterfaces.put(DozyVoD.libraryDozyName, (DozySyncI) librarySyncIComp.getComponent());
//        synchronousInterfaces.put(DozyVoD.torrentDozyName, (DozySyncI) torrentSyncIComp.getComponent());
//
//        List<DozyResource> resources = new ArrayList<>();
//        resources.add(new VoDEndpointREST());
//        resources.add(new HopsConnectionREST());
//        resources.add(new ContentsSummaryREST());
//        resources.add(new TorrentExtendedStatusREST());
//        resources.add(new HopsTorrentDownloadREST());
//        resources.add(new HopsTorrentUploadREST());
//        resources.add(new HopsTorrentStopREST());
//        resources.add(new HopsFileDeleteREST());
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
