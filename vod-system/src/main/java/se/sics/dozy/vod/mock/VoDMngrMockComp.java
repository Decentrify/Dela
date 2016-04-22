/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * GVoD is free software; you can redistribute it and/or
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
package se.sics.dozy.vod.mock;

import java.util.HashMap;
import java.util.Map;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.gvod.mngr.LibraryPort;
import se.sics.gvod.mngr.TorrentPort;
import se.sics.gvod.mngr.VideoPort;
import se.sics.gvod.mngr.event.LibraryAddEvent;
import se.sics.gvod.mngr.event.TorrentDownloadEvent;
import se.sics.gvod.mngr.event.LibraryContentsEvent;
import se.sics.gvod.mngr.event.LibraryElementEvent;
import se.sics.gvod.mngr.event.TorrentStopEvent;
import se.sics.gvod.mngr.event.TorrentUploadEvent;
import se.sics.gvod.mngr.event.VideoPlayEvent;
import se.sics.gvod.mngr.event.VideoStopEvent;
import se.sics.gvod.mngr.util.FileInfo;
import se.sics.gvod.mngr.util.TorrentInfo;
import se.sics.gvod.mngr.util.TorrentStatus;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.IntIdentifier;
import se.sics.ktoolbox.util.network.KAddress;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class VoDMngrMockComp extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(VoDMngrMockComp.class);
    private String logPrefix = "";

    //***************************CONNECTIONS************************************
    Negative<LibraryPort> libraryPort = provides(LibraryPort.class);
    Negative<TorrentPort> torrentPort = provides(TorrentPort.class);
    Negative<VideoPort> videoPort = provides(VideoPort.class);
    //**************************INTERNAL_STATE**********************************
    private Map<Identifier, Pair<FileInfo, TorrentInfo>> libraryContents = new HashMap<>();

    public VoDMngrMockComp(Init init) {
        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control);
        subscribe(handleLibraryContent, libraryPort);
        subscribe(handleLibraryElement, libraryPort);
        subscribe(handleLibraryAdd, libraryPort);
        subscribe(handleTorrentUpload, torrentPort);
        subscribe(handleTorrentDownload, torrentPort);
        subscribe(handleTorrentStop, torrentPort);
        subscribe(handleVideoPlay, videoPort);
        subscribe(handleVideoStop, videoPort);
    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);

            libraryContents.put(new IntIdentifier(1), Pair.with(
                    new FileInfo("test1", "/root/test1", 1024, "test file 1"),
                    TorrentInfo.none()));
            libraryContents.put(new IntIdentifier(2), Pair.with(
                    new FileInfo("test2", "/root/test2", 2024, "test file 2"),
                    TorrentInfo.none()));
        }
    };

    Handler handleLibraryContent = new Handler<LibraryContentsEvent.Request>() {
        @Override
        public void handle(LibraryContentsEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            LibraryContentsEvent.Response resp = req.success(libraryContents);
            LOG.info("{}answering:{}", logPrefix, resp);
            answer(req, resp);
        }
    };

    Handler handleLibraryElement = new Handler<LibraryElementEvent.Request>() {
        @Override
        public void handle(LibraryElementEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            if (libraryContents.containsKey(req.overlayId)) {
                Pair<FileInfo, TorrentInfo> elementInfo = libraryContents.get(req.overlayId);
                LibraryElementEvent.Response resp = req.success(elementInfo.getValue0(), elementInfo.getValue1());
                LOG.info("{}answering:{}", logPrefix, resp);
                answer(req, resp);
            } else {
                LibraryElementEvent.Response resp = req.badRequest("missing library element");
                LOG.info("{}answering:{}", logPrefix, resp);
                answer(req, resp);
            }
        }
    };
    
    Handler handleLibraryAdd = new Handler<LibraryAddEvent.Request>() {
        @Override
        public void handle(LibraryAddEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            if (!libraryContents.containsKey(req.overlayId)) {
                Map<Identifier, KAddress> partners = new HashMap<>();
                TorrentInfo torrentInfo = new TorrentInfo(TorrentStatus.NONE, partners, 0, 0, 0);
                libraryContents.put(req.overlayId, Pair.with(req.fileInfo, torrentInfo));
                LibraryAddEvent.Response resp = req.success();
                LOG.info("{}answering:{}", logPrefix, resp);
                answer(req, resp);
            } else {
                LibraryAddEvent.Response resp = req.badRequest("element already in library");
                LOG.info("{}answering:{}", logPrefix, resp);
                answer(req, resp);
            }
        }
    };

    Handler handleTorrentUpload = new Handler<TorrentUploadEvent.Request>() {
        @Override
        public void handle(TorrentUploadEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            TorrentUploadEvent.Response resp;
            if (libraryContents.containsKey(req.overlayId)) {
                Pair<FileInfo, TorrentInfo> elementInfo = libraryContents.get(req.overlayId);
                if (!elementInfo.getValue1().status.equals(TorrentStatus.NONE)) {
                    resp = req.badRequest("bad status");
                } else {
                    Map<Identifier, KAddress> partners = new HashMap<>();
                    TorrentInfo torrentInfo = new TorrentInfo(TorrentStatus.UPLOADING, partners, 1, 0, 0);
                    libraryContents.put(req.overlayId, Pair.with(elementInfo.getValue0(), torrentInfo));
                    resp = req.success();
                }
            } else {
                resp = req.badRequest("missing");
            }
            LOG.info("{}answering:{}", logPrefix, resp);
            answer(req, resp);
        }
    };

    Handler handleTorrentDownload = new Handler<TorrentDownloadEvent.Request>() {
        @Override
        public void handle(TorrentDownloadEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            TorrentDownloadEvent.Response resp;
            if (!libraryContents.containsKey(req.overlayId)) {
                FileInfo fileInfo = new FileInfo(req.fileName, "", 0, "");
                Map<Identifier, KAddress> partners = new HashMap<>();
                TorrentInfo torrentInfo = new TorrentInfo(TorrentStatus.DOWNLOADING, partners, 0, 0, 0);
                libraryContents.put(req.overlayId, Pair.with(fileInfo, torrentInfo));
                resp = req.success();
            } else {
                resp = req.badRequest("file exists");
            }
            LOG.info("{}answering:{}", logPrefix, resp);
            answer(req, resp);
        }
    };

    Handler handleTorrentStop = new Handler<TorrentStopEvent.Request>() {
        @Override
        public void handle(TorrentStopEvent.Request req) {
            LOG.info("{}received:{}", logPrefix, req);
            TorrentStopEvent.Response resp;
            if (libraryContents.containsKey(req.overlayId)) {
                Pair<FileInfo, TorrentInfo> elementInfo = libraryContents.get(req.overlayId);
                switch (elementInfo.getValue1().status) {
                    case NONE:
                        resp = req.badRequest("bad status");
                        break;
                    case UPLOADING:
                        Map<Identifier, KAddress> partners = new HashMap<>();
                        TorrentInfo torrentInfo = new TorrentInfo(TorrentStatus.NONE, partners, 0, 0, 0);
                        libraryContents.put(req.overlayId, Pair.with(elementInfo.getValue0(), torrentInfo));
                        resp = req.success();
                        break;
                    case DOWNLOADING:
                        libraryContents.remove(req.overlayId);
                        resp = req.success();
                        break;
                    default:
                        resp = req.badRequest("mock logic error");
                }
            } else {
                resp = req.badRequest("missing");
            }
            LOG.info("{}answering:{}", logPrefix, resp);
            answer(req, resp);
        }
    };

    Handler handleVideoPlay = new Handler<VideoPlayEvent.Request>() {
        @Override
        public void handle(VideoPlayEvent.Request event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    Handler handleVideoStop = new Handler<VideoStopEvent.Request>() {
        @Override
        public void handle(VideoStopEvent.Request event) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    public static class Init extends se.sics.kompics.Init<VoDMngrMockComp> {
    }
}
