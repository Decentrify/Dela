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
package se.sics.dozy.vod.model.hops.util;

import se.sics.nstream.hops.kafka.KafkaEndpoint;

/**
 *
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class KafkaEndpointJSON {
    private String brokerEndpoint;
    private String restEndpoint;
    private String domain;
    private String projectId;
    private String keyStore;
    private String trustStore;

    public String getBrokerEndpoint() {
        return brokerEndpoint;
    }

    public void setBrokerEndpoint(String brokerEndpoint) {
        this.brokerEndpoint = brokerEndpoint;
    }

    public String getRestEndpoint() {
        return restEndpoint;
    }

    public void setRestEndpoint(String restEndpoint) {
        this.restEndpoint = restEndpoint;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public KafkaEndpoint resolve() {
        return new KafkaEndpoint(brokerEndpoint, restEndpoint, domain, projectId, keyStore, trustStore);
    }
}
