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
package se.sics.dozy.vod.model.hops;

import se.sics.ktoolbox.kafka.KafkaResource;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class KafkaResourceJSON {

    private String brokerEndpoint;
    private String restEndpoint;
    private String domain;
    private String sessionId;
    private String projectId;
    private String topicName;
    private String schemaName;
    private String keyStore;
    private String trustStore;

    public KafkaResourceJSON(String brokerEndpoint, String restEndpoint, String domain, String sessionId,
            String projectId, String topicName, String schemaName, String keyStore, String trustStore) {
        this.brokerEndpoint = brokerEndpoint;
        this.restEndpoint = restEndpoint;
        this.domain = domain;
        this.sessionId = sessionId;
        this.projectId = projectId;
        this.topicName = topicName;
        this.schemaName = schemaName;
        this.keyStore = keyStore;
        this.trustStore = trustStore;
    }

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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
    
    public static KafkaResource fromJSON(KafkaResourceJSON json) {
        return new KafkaResource(json.brokerEndpoint, json.restEndpoint, json.domain, json.sessionId, json.projectId, json.topicName, json.schemaName, json.keyStore, json.trustStore);
    }
}
