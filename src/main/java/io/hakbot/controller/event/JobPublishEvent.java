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
package io.hakbot.controller.event;

import alpine.event.framework.Event;

/**
 * This event is called when a completed job needs publishing.
 *
 * @see io.hakbot.controller.workers.JobPublishWorker
 */
public class JobPublishEvent extends BaseJobEvent implements Event {

    private String result;

    public JobPublishEvent(String jobUuid) {
        super(jobUuid);
    }

    public String getResult() {
        return result;
    }

    public JobPublishEvent result(String result) {
        this.result = result;
        return this;
    }

}
