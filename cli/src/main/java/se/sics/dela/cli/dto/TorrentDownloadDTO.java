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
package se.sics.dela.cli.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TorrentDownloadDTO {

  public static class Start {

    private TorrentId torrentId;
    private String torrentName;
    private Integer projectId;
    private Integer datasetId;
    private HDFSResource manifestHDFSResource;
    private List<AddressJSON> partners;
    private HDFSEndpoint hdfsEndpoint;

    public Start(TorrentId torrentId, String torrentName,
      Integer projectId, Integer datasetId, HDFSResource manifestHDFSResource,
      List<AddressJSON> partners, HDFSEndpoint hdfsEndpoint) {
      this.torrentId = torrentId;
      this.torrentName = torrentName;
      this.projectId = projectId;
      this.datasetId = datasetId;
      this.manifestHDFSResource = manifestHDFSResource;
      this.partners = partners;
      this.hdfsEndpoint = hdfsEndpoint;
    }

    public Start() {
    }

    public String getTorrentName() {
      return torrentName;
    }

    public void setTorrentName(String torrentName) {
      this.torrentName = torrentName;
    }

    public TorrentId getTorrentId() {
      return torrentId;
    }

    public void setTorrentId(TorrentId torrentId) {
      this.torrentId = torrentId;
    }

    public Integer getProjectId() {
      return projectId;
    }

    public void setProjectId(Integer projectId) {
      this.projectId = projectId;
    }

    public Integer getDatasetId() {
      return datasetId;
    }

    public void setDatasetId(Integer datasetId) {
      this.datasetId = datasetId;
    }

    public HDFSResource getManifestHDFSResource() {
      return manifestHDFSResource;
    }

    public void setManifestHDFSResource(HDFSResource manifestHDFSResource) {
      this.manifestHDFSResource = manifestHDFSResource;
    }

    public List<AddressJSON> getPartners() {
      return partners;
    }

    public void setPartners(List<AddressJSON> partners) {
      this.partners = partners;
    }

    public HDFSEndpoint getHdfsEndpoint() {
      return hdfsEndpoint;
    }

    public void setHdfsEndpoint(HDFSEndpoint hdfsEndpoint) {
      this.hdfsEndpoint = hdfsEndpoint;
    }
  }

  @XmlRootElement
  public static class Advance {

    private TorrentId torrentId;
    private Object kafkaEndpoint;
    private HDFSEndpoint hdfsEndpoint;
    private ExtendedDetails extendedDetails;

    public Advance() {
    }

    public Advance(TorrentId torrentId, HDFSEndpoint hdfsEndpoint, ExtendedDetails extendedDetails) {
      this.torrentId = torrentId;
      this.kafkaEndpoint = null;
      this.hdfsEndpoint = hdfsEndpoint;
      this.extendedDetails = extendedDetails;
    }

    public TorrentId getTorrentId() {
      return torrentId;
    }

    public void setTorrentId(TorrentId torrentId) {
      this.torrentId = torrentId;
    }

    public Object getKafkaEndpoint() {
      return kafkaEndpoint;
    }

    public void setKafkaEndpoint(Object kafkaEndpoint) {
      this.kafkaEndpoint = kafkaEndpoint;
    }

    public ExtendedDetails getExtendedDetails() {
      return extendedDetails;
    }

    public void setExtendedDetails(ExtendedDetails extendedDetails) {
      this.extendedDetails = extendedDetails;
    }

    public HDFSEndpoint getHdfsEndpoint() {
      return hdfsEndpoint;
    }

    public void setHdfsEndpoint(HDFSEndpoint hdfsEndpoint) {
      this.hdfsEndpoint = hdfsEndpoint;
    }
  }
}
