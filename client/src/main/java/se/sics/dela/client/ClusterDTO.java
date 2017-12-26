package se.sics.dela.client;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.bind.annotation.XmlRootElement;
import se.sics.kompics.util.Identifier;
import se.sics.ktoolbox.util.identifiable.BasicBuilders;
import se.sics.ktoolbox.util.identifiable.BasicIdentifiers;
import se.sics.ktoolbox.util.identifiable.IdentifierFactory;
import se.sics.ktoolbox.util.identifiable.IdentifierRegistry;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.basic.BasicAddress;
import se.sics.ktoolbox.util.network.nat.NatAwareAddressImpl;
import se.sics.ktoolbox.util.network.nat.NatType;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ClusterDTO {

  @XmlRootElement
  public class ClusterAddress implements Serializable {

    private String clusterId;
    private String delaTransferAddress;
    private String delaClusterAddress;

    public ClusterAddress() {
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

    @Override
    public String toString() {
      return "CAdr{" + "cId=" + clusterId + ", dTAdr=" + delaTransferAddress + ", dCAdr=" + delaClusterAddress + '}';
    }
  }

  @XmlRootElement
  public class DelaAddress implements Serializable {

    private String ip;
    private int port;
    private String id;

    public DelaAddress() {
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public int getPort() {
      return port;
    }

    public void setPort(int port) {
      this.port = port;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

//    public String getNat() {
//      return nat;
//    }
//
//    public void setNat(String nat) {
//      this.nat = nat;
//    }

    public KAddress resolve() {
      try {
        IdentifierFactory nodeIdFactory = IdentifierRegistry.lookup(BasicIdentifiers.Values.NODE.toString());
        Identifier nodeId = nodeIdFactory.id(new BasicBuilders.IntBuilder(Integer.valueOf(id)));
//        Optional<NatType> natType = NatType.decode(nat);
//        if (!natType.isPresent()) {
//          throw new RuntimeException("unknown nat");
//        }
        BasicAddress publicAdr = new BasicAddress(InetAddress.getByName(ip), port, nodeId);
//        KAddress adr = NatAwareAddressImpl.adr(publicAdr, natType.get());
        KAddress adr = NatAwareAddressImpl.adr(publicAdr, NatType.open());
        return adr;
      } catch (UnknownHostException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
