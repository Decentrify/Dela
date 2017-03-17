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
package se.sics.dozy.vod.hops.torrent.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.javatuples.Pair;
import se.sics.dozy.vod.model.hops.util.HDFSResourceJSON;
import se.sics.dozy.vod.model.hops.util.KafkaResourceJSON;
import se.sics.nstream.hops.kafka.KafkaResource;
import se.sics.nstream.hops.storage.hdfs.HDFSResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ExtendedDetailsJSON {

  public static class Raw {
    private List<RawHdfs> hdfsDetails;
    private List<RawKafka> kafkaDetails;

    public Raw() {
    }

    public Raw(List<RawHdfs> hdfsDetails, List<RawKafka> kafkaDetails) {
      this.hdfsDetails = hdfsDetails;
      this.kafkaDetails = kafkaDetails;
    }

    public List<RawHdfs> getHdfsDetails() {
      return hdfsDetails;
    }

    public void setHdfsDetails(List<RawHdfs> hdfsDetails) {
      this.hdfsDetails = hdfsDetails;
    }

    public List<RawKafka> getKafkaDetails() {
      return kafkaDetails;
    }

    public void setKafkaDetails(List<RawKafka> kafkaDetails) {
      this.kafkaDetails = kafkaDetails;
    }
    
    public Pair<Map<String, HDFSResource>, Map<String, KafkaResource>> resolve() {
      Map<String, HDFSResource> hdfsResult = new HashMap<>();
      for (RawHdfs e : hdfsDetails) {
        hdfsResult.put(e.getFile(), e.getResource().resolve());
      }

      Map<String, KafkaResource> kafkaResult = new HashMap<>();
      for (RawKafka e : kafkaDetails) {
        kafkaResult.put(e.getFile(), e.getResource().resolve());
      }

      return Pair.with(hdfsResult, kafkaResult);
    }
  }
  
  private static class RawHdfs {
    private String file;
    private HDFSResourceJSON resource;

    public String getFile() {
      return file;
    }

    public void setFile(String file) {
      this.file = file;
    }

    public HDFSResourceJSON getResource() {
      return resource;
    }

    public void setResource(HDFSResourceJSON resource) {
      this.resource = resource;
    }
  }
  
  private static class RawKafka {
    private String file;
    private KafkaResourceJSON resource;

    public String getFile() {
      return file;
    }

    public void setFile(String file) {
      this.file = file;
    }

    public KafkaResourceJSON getResource() {
      return resource;
    }

    public void setResource(KafkaResourceJSON resource) {
      this.resource = resource;
    }
  }
}
