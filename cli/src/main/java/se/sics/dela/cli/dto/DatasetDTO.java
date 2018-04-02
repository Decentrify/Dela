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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DatasetDTO {
  @XmlRootElement
  public static class Search implements Serializable {
    private String name;
    private int version;
    private String description;

    public Search() {
    }

    public Search(String name, int version, String description) {
      this.name = name;
      this.version = version;
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getVersion() {
      return version;
    }

    public void setVersion(int version) {
      this.version = version;
    }
    
    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }
  }
  
  @XmlRootElement
  public static class Details implements Serializable {
    private Owner owner;
    private Collection<String> categories;
    private Date publishedOn;
    private long size;
    private Health datasetHealth;

    public Details() {
    }

    public Owner getOwner() {
      return owner;
    }

    public void setOwner(Owner owner) {
      this.owner = owner;
    }

    public Collection<String> getCategories() {
      return categories;
    }

    public void setCategories(Collection<String> categories) {
      this.categories = categories;
    }

    public Date getPublishedOn() {
      return publishedOn;
    }

    public void setPublishedOn(Date publishedOn) {
      this.publishedOn = publishedOn;
    }

    public long getSize() {
      return size;
    }

    public void setSize(long size) {
      this.size = size;
    }

    public Health getDatasetHealth() {
      return datasetHealth;
    }

    public void setDatasetHealth(Health datasetHealth) {
      this.datasetHealth = datasetHealth;
    }
  }
  
  @XmlRootElement
  public static class Owner{
    private String clusterDescription;
    private String userDescription;

    public Owner() {
    }

    public String getClusterDescription() {
      return clusterDescription;
    }

    public void setClusterDescription(String clusterDescription) {
      this.clusterDescription = clusterDescription;
    }

    public String getUserDescription() {
      return userDescription;
    }

    public void setUserDescription(String userDescription) {
      this.userDescription = userDescription;
    }
  }
  
  @XmlRootElement
  public static class Health implements Serializable {
    private int seeders;
    private int leechers;

    public Health() {
    }

    public int getSeeders() {
      return seeders;
    }

    public void setSeeders(int seeders) {
      this.seeders = seeders;
    }

    public int getLeechers() {
      return leechers;
    }

    public void setLeechers(int leechers) {
      this.leechers = leechers;
    }
  }
}
