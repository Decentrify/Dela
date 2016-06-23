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
package se.sics.dozy.vod.model;

import java.io.UnsupportedEncodingException;
import se.sics.gvod.mngr.util.TorrentExtendedStatus;
import se.sics.ktoolbox.util.identifiable.basic.ByteIdentifier;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TorrentExtendedStatusJSON {

    private TorrentIdJSON torrentId;
    private String torrentStatus;
    private int downloadSpeed;
    private int percentageCompleted;

    public TorrentExtendedStatusJSON(TorrentIdJSON torrentId, String torrentStatus, int downloadSpeed,
            int percentageCompleted) {
        this.torrentId = torrentId;
        this.torrentStatus = torrentStatus;
        this.downloadSpeed = downloadSpeed;
        this.percentageCompleted = percentageCompleted;
    }

    public TorrentExtendedStatusJSON() {
    }

    public String getTorrentStatus() {
        return torrentStatus;
    }

    public void setTorrentStatus(String torrentStatus) {
        this.torrentStatus = torrentStatus;
    }

    public int getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(int downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public TorrentIdJSON getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(TorrentIdJSON torrentId) {
        this.torrentId = torrentId;
    }

    public int getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(int percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public static TorrentExtendedStatusJSON resolveToJson(TorrentExtendedStatus tes) {
        return new TorrentExtendedStatusJSON(TorrentIdJSON.toJSON(tes.torrentId), tes.torrentStatus.name(),
                tes.downloadSpeed, tes.percentageComplete);
    }
}
