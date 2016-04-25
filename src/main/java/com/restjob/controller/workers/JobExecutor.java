/*
 * This file is part of RESTjob Controller.
 *
 * RESTjob Controller is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * RESTjob Controller is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * RESTjob Controller. If not, see http://www.gnu.org/licenses/.
 */
package com.restjob.controller.workers;

import com.restjob.controller.logging.Logger;
import com.restjob.controller.model.Job;
import com.restjob.controller.providers.BaseProvider;

import javax.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

/**
 * This class is responsible for executing jobs.
 */
public class JobExecutor implements Runnable {

    // Setup logging
    private static final Logger logger = Logger.getLogger(JobExecutor.class);

    private Job job;
    private boolean isExecuting = true;
    private EntityManager em;
    private BaseProvider provider;

    /**
     * Constructs a new JobExecutor instance for the specified Job
     * @param job A Job object
     */
    public JobExecutor(EntityManager em, Job job) {
        this.em = em;
        this.job = job;
    }

    /**
     * Executes the job.
     */
    public void run() {
        if (job.getUuid() == null || job.getPayload() == null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Job: " + job.getUuid() + " is being executed.");
        }

        em.getTransaction().begin();
        job.setStarted(new Date());
        em.getTransaction().commit();

        boolean success = false;
        String result = null;
        try {
            Class clazz = Class.forName(job.getProvider(), false, this.getClass().getClassLoader());
            //if (clazz.isAssignableFrom(Controllable.class)) { /
            // /todo - ensure only classes that extend BaseProvider or implement Controllable are allowed
                Constructor<?> constructor = clazz.getConstructor();
                this.provider = (BaseProvider) constructor.newInstance();
                provider.process(job);
                success = job.getSuccess();
                result = provider.getResult();
            //}
        } catch (ClassNotFoundException | NoSuchMethodException |
                IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error(e.getMessage());
        } finally {
            em.getTransaction().begin();
            job.setResult(result);
            job.setState(State.COMPLETED);
            job.setCompleted(new Date());
            job.setSuccess(success);
            em.getTransaction().commit();
            isExecuting = false;
            if (logger.isDebugEnabled()) {
                logger.debug(job.getUuid() + " - Provider: " + job.getProvider());
                logger.debug(job.getUuid() + " - State: " + job.getState());
                logger.debug(job.getUuid() + " - Success: " + job.getSuccess());
            }
        }
    }

    /**
     * Cancels the job
     */
    public void cancel() {
        if (provider != null) {
            provider.cancel();
        }
    }

    /**
     * Return the Job currently being executed
     * @return the Job object being executed
     */
    public Job getJob() {
        return job;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void waitFor() {
        while (isExecuting()) {
            // do nothing
        }
    }

    public Thread getThread() {
        return Thread.currentThread();
    }

}