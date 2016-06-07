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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import se.sics.gvod.mngr.util.TorrentInfo;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.identifiable.basic.IntIdentifier;
import se.sics.ktoolbox.util.identifiable.basic.OverlayIdentifier;
import se.sics.ktoolbox.util.network.KAddress;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TorrentInfoJSON {

    private Map<Integer, InetAddress> partners = new HashMap<>();
    private double progress; //percentage
    private long downloadSpeed; //byte/s
    private long uploadSpeed; //byte/s

    public TorrentInfoJSON(Map<Integer, InetAddress> partners, double progress, long downloadSpeed, long uploadSpeed) {
        this.partners = partners;
        this.progress = progress;
        this.downloadSpeed = downloadSpeed;
        this.uploadSpeed = uploadSpeed;
    }

    public TorrentInfoJSON() {
    }

    public Map<Integer, InetAddress> getPartners() {
        return partners;
    }

    public void setPartners(Map<Integer, InetAddress> partners) {
        this.partners = partners;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public long getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(long uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }
    
    public static TorrentInfoJSON resolve(TorrentInfo torrentInfo) {
        if(torrentInfo == null) {
            return null;
        }
        Map<Integer, InetAddress> partners = new HashMap<>();
        for(Map.Entry<Identifier, KAddress> e : torrentInfo.partners.entrySet()) {
            partners.put(((OverlayIdentifier)e.getKey()).getInt(), e.getValue().getIp());
        }
        return new TorrentInfoJSON(partners, torrentInfo.progress, torrentInfo.downloadSpeed, torrentInfo.uploadSpeed);
    }
}