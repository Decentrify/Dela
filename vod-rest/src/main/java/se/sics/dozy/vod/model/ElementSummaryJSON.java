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

import se.sics.nstream.library.util.TorrentState;
import se.sics.nstream.mngr.util.ElementSummary;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public abstract class ElementSummaryJSON {

  private String fileName;
  private TorrentIdJSON torrentId;
  private String torrentStatus;

  private ElementSummaryJSON(String name, TorrentIdJSON torrentId, String status) {
    this.fileName = name;
    this.torrentId = torrentId;
    this.torrentStatus = status;
  }

  public ElementSummaryJSON() {
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public TorrentIdJSON getTorrentId() {
    return torrentId;
  }

  public void setTorrentId(TorrentIdJSON torrentId) {
    this.torrentId = torrentId;
  }

  public String getTorrentStatus() {
    return torrentStatus;
  }

  public void setTorrentStatus(String torrentStatus) {
    this.torrentStatus = torrentStatus;
  }

  @Override
  public String toString() {
    return "ElementSummaryJSON{" + "fileName=" + fileName + ", torrentId=" + torrentId + ", torrentStatus="
      + torrentStatus + '}';
  }

  public static ElementSummaryJSON resolve(ElementSummary es) {
    if (es.status.equals(TorrentState.DOWNLOADING)) {
      ElementSummary.Download d = (ElementSummary.Download)es;
      return new ElementSummaryJSON.Download(es.fileName, TorrentIdJSON.toJSON(es.torrentId), es.status.name(),
        d.speed, d.dynamic);
    } else {
      return new ElementSummaryJSON.Upload(es.fileName, TorrentIdJSON.toJSON(es.torrentId), es.status.name());
    }
  }

  public static class Download extends ElementSummaryJSON {

    private long speed;
    private double dynamic;

    public Download(String name, TorrentIdJSON torrentId, String status, long speed, double dynamic) {
      super(name, torrentId, status);
      this.speed = speed;
      this.dynamic = dynamic;
    }

    public long getSpeed() {
      return speed;
    }

    public void setSpeed(long speed) {
      this.speed = speed;
    }

    public double getDynamic() {
      return dynamic;
    }

    public void setDynamic(double dynamic) {
      this.dynamic = dynamic;
    }
  }

  public static class Upload extends ElementSummaryJSON {

    public Upload(String name, TorrentIdJSON torrentId, String status) {
      super(name, torrentId, status);
    }
  }
}
