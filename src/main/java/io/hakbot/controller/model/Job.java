/*
 * This file is part of Hakbot Origin Controller.
 *
 * Hakbot Origin Controller is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Hakbot Origin Controller is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hakbot Origin Controller. If not, see http://www.gnu.org/licenses/.
 */
package io.hakbot.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.hakbot.controller.workers.State;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import java.io.Serializable;
import java.util.Date;

@PersistenceCapable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job implements Serializable {

    private static final long serialVersionUID = 4247510467373253623L;

    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    @JsonIgnore
    private long id;

    @Persistent
    @Unique(name="JOB_UUID_IDX")
    @Column(name="UUID", jdbcType="VARCHAR", length=36, allowsNull="false")
    private String uuid;

    @Persistent
    @Column(name="NAME", jdbcType="VARCHAR", length=50, allowsNull="false")
    private String name;

    @Persistent
    @Column(name="PROVIDER", jdbcType="VARCHAR", length=255, allowsNull="false")
    private String provider;

    @Persistent
    @Column(name="PUBLISHER", jdbcType="VARCHAR", length=255)
    private String publisher;

    @Persistent(defaultFetchGroup="true")
    @Column(name="MESSAGE", jdbcType="CLOB")
    private String message;

    @Persistent
    @Column(name="CREATED", jdbcType="TIMESTAMP", allowsNull="false")
    private Date created;

    @Persistent
    @Column(name="STARTED", jdbcType="TIMESTAMP")
    private Date started;

    @Persistent
    @Column(name="COMPLETED", jdbcType="TIMESTAMP")
    private Date completed;

    @NotPersistent
    private Long duration;

    @Persistent
    @Column(name="STARTED_BY_APIKEY_ID")
    private long apiKeyId;

    @Persistent
    @Column(name="STATE", jdbcType="VARCHAR", length=20, allowsNull="false")
    private String state;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreated() {
        if (created != null) {
            return new Date(created.getTime());
        }
        return null;
    }

    public void setCreated(Date created) {
        if (created == null) {
            this.created = null;
        } else {
            this.created = new Date(created.getTime());
        }
    }

    public Date getStarted() {
        if (started != null) {
            return new Date(started.getTime());
        }
        return null;
    }

    public void setStarted(Date started) {
        if (started == null) {
            this.started = null;
        } else {
            this.started = new Date(started.getTime());
        }
    }

    public Date getCompleted() {
        if (completed != null) {
            return new Date(completed.getTime());
        }
        return null;
    }

    public void setCompleted(Date completed) {
        if (completed == null) {
            this.completed = null;
        } else {
            this.completed = new Date(completed.getTime());
        }
    }

    public Long getDuration() {
        if (created != null && created.getTime() > 0 && completed != null && completed.getTime() > created.getTime()) {
            return completed.getTime() - created.getTime();
        }
        return null;
    }

    public long getStartedByApiKeyId() {
        return apiKeyId;
    }

    public void setStartedByApiKeyId(long apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public State getState () {
        return State.parse(this.state);
    }

    public void setState(State state) {
        this.state = state.getValue();
    }

}
