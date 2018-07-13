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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
@XmlRootElement
public class ClusterAddressDTO {
  private String clusterId;
  private String delaTransferAddress;
  private String delaClusterAddress;

  public ClusterAddressDTO() {
  }

  public String getClusterId() {
    return clusterId;
  }

  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  public String getDelaTransferAddress() {
    return delaTransferAddress;
  }

  public void setDelaTransferAddress(String delaTransferAddress) {
    this.delaTransferAddress = delaTransferAddress;
  }

  public String getDelaClusterAddress() {
    return delaClusterAddress;
  }

  public void setDelaClusterAddress(String delaClusterAddress) {
    this.delaClusterAddress = delaClusterAddress;
  }
}
