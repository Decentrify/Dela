package se.sics.dela.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class DatasetDTO {
  @XmlRootElement
  public static class Owner implements Serializable {

    private String clusterDescription;
    private String userDescription;

    public Owner() {
    }

    public Owner(String clusterDescription, String userDescription) {
      this.clusterDescription = clusterDescription;
      this.userDescription = userDescription;
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

    public Health(int seeders, int leechers) {
      this.seeders = seeders;
      this.leechers = leechers;
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

  @XmlRootElement
  public static class Details implements Serializable {

    private Owner owner;
    private Collection<String> categories;
    private Date publishedOn;
    private long size;
    private Health datasetHealth;

    public Details() {
    }

    public Details(Owner owner, Collection<String> categories, Date publishedOn, long size, Health datasetHealth) {
      this.owner = owner;
      this.categories = categories;
      this.publishedOn = publishedOn;
      this.size = size;
      this.datasetHealth = datasetHealth;
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
}
