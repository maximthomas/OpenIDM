/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Portions copyright 2012-2016 ForgeRock AS.
 */

package org.forgerock.openidm.scheduler;

import static org.forgerock.json.JsonValue.*;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.util.DateUtil;
import org.forgerock.openidm.scheduler.impl.TriggerType;
import org.joda.time.DateTimeZone;

public class ScheduleConfig {

    /**
     * The time format that the scheduler config expects. This is basically ISO-8601 format with the timezone removed.
     * The timezone is then specified in the timeZone field. It would be best if this format was removed and startTime
     * and endTime were full ISO-8601. The timezone could then be extracted from the start or end time string.
     *
     * Please see https://bugster.forgerock.org/jira/browse/OPENIDM-6712.
     */
    private static final String SCHEDULER_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private Boolean enabled = null;
    private Boolean persisted = null;
    private String misfirePolicy = null;
    private TriggerType triggerType;
    private Date startTime = null;
    private Date endTime = null;
    private String cronSchedule = null;
    private TimeZone timeZone = null;
    private String invokeService = null;
    private Object invokeContext = null;
    private String invokeLogLevel = null;
    private Boolean concurrentExecution = null;

    private final DateUtil dateUtil;

    public ScheduleConfig(JsonValue config) throws ResourceException {
        JsonValue enabledValue = config.get(SchedulerService.SCHEDULE_ENABLED);
        if (enabledValue.isString()) {
            enabled = Boolean.parseBoolean(enabledValue.defaultTo("true").asString());
        } else {
            enabled = enabledValue.defaultTo(Boolean.TRUE).asBoolean();
        }
        JsonValue persistedValue = config.get(SchedulerService.SCHEDULE_PERSISTED);
        if (persistedValue.isString()) {
            persisted = Boolean.parseBoolean(persistedValue.defaultTo("false").asString());
        } else {
            persisted = persistedValue.defaultTo(Boolean.FALSE).asBoolean();
        }
        JsonValue concurrentExecutionValue = config.get(SchedulerService.SCHEDULE_CONCURRENT_EXECUTION);
        if (concurrentExecutionValue.isString()) {
            concurrentExecution = Boolean.parseBoolean(concurrentExecutionValue.defaultTo("false").asString());
        } else {
            concurrentExecution = concurrentExecutionValue.defaultTo(Boolean.FALSE).asBoolean();
        }
        misfirePolicy = config.get(SchedulerService.SCHEDULE_MISFIRE_POLICY).defaultTo(SchedulerService.MISFIRE_POLICY_FIRE_AND_PROCEED).asString();
        cronSchedule = config.get(SchedulerService.SCHEDULE_CRON_SCHEDULE).asString();
        final String configTriggerType = config.get(SchedulerService.SCHEDULE_TYPE).asString();
        if (StringUtils.isNotBlank(configTriggerType)) {
            try {
                triggerType = TriggerType.valueOf(configTriggerType);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Specified type value invalid: " + configTriggerType);
            }
        } else {
            triggerType = TriggerType.cron;
        }
        if (TriggerType.cron == triggerType) {
            if (StringUtils.isBlank(cronSchedule)) {
                throw new BadRequestException("A schedule of type cron must provide a cron schedule");
            }
            if (!misfirePolicy.equals(SchedulerService.MISFIRE_POLICY_FIRE_AND_PROCEED) &&
                    !misfirePolicy.equals(SchedulerService.MISFIRE_POLICY_DO_NOTHING)) {
                throw new BadRequestException(new StringBuilder("Invalid misfire policy: ").append(misfirePolicy).toString());
            }
        }
        invokeService = config.get(SchedulerService.SCHEDULE_INVOKE_SERVICE).asString();
        if (!StringUtils.isNotBlank(invokeService)) {
            throw new BadRequestException("Invalid scheduler configuration, the "
                    + SchedulerService.SCHEDULE_INVOKE_SERVICE
                    + " property needs to be set but is empty. "
                    + "Complete config:" + config);
        } else {
            // service PIDs fragments are prefixed with openidm qualifier
            if (!invokeService.contains(".")) {
                String fragment = invokeService;
                invokeService = ServerConstants.SERVICE_RDN_PREFIX + fragment;
            }
        }
        invokeContext = config.get(SchedulerService.SCHEDULE_INVOKE_CONTEXT).getObject();
        invokeLogLevel = config.get(SchedulerService.SCHEDULE_INVOKE_LOG_LEVEL).defaultTo("info").asString();
        String timeZoneString = config.get(SchedulerService.SCHEDULE_TIME_ZONE).asString();
        String startTimeString = config.get(SchedulerService.SCHEDULE_START_TIME).asString();
        String endTimeString = config.get(SchedulerService.SCHEDULE_END_TIME).asString();
        if (StringUtils.isNotBlank(timeZoneString)) {
            timeZone = TimeZone.getTimeZone(timeZoneString);
            // JDK has fall-back behavior to GMT if it doesn't understand timezone passed
            if (!timeZoneString.equals(timeZone.getID())) {
                throw new BadRequestException("Scheduler configured timezone is not understood: " + timeZoneString);
            }
        }
        dateUtil = timeZone != null
                ? DateUtil.getDateUtil(DateTimeZone.forTimeZone(timeZone))
                :  DateUtil.getDateUtil();
        if (StringUtils.isNotBlank(startTimeString)) {
            startTime = dateUtil.parseTime(startTimeString, SCHEDULER_TIME_FORMAT);
            // TODO: enhanced logging for failure
        }

        if (StringUtils.isNotBlank(endTimeString)) {
            endTime = dateUtil.parseTime(endTimeString, SCHEDULER_TIME_FORMAT);
            // TODO: enhanced logging for failure
        }
    }

    public JsonValue getConfig() {
        return json(object(field(SchedulerService.SCHEDULE_ENABLED, isEnabled()),
                field(SchedulerService.SCHEDULE_PERSISTED, isPersisted()),
                field(SchedulerService.SCHEDULE_MISFIRE_POLICY, getMisfirePolicy()),
                field(SchedulerService.SCHEDULE_CRON_SCHEDULE, getCronSchedule()),
                field(SchedulerService.SCHEDULE_TYPE, getScheduleType()),
                field(SchedulerService.SCHEDULE_INVOKE_SERVICE, getInvokeService()),
                field(SchedulerService.SCHEDULE_INVOKE_CONTEXT, getInvokeContext()),
                field(SchedulerService.SCHEDULE_INVOKE_LOG_LEVEL, getInvokeLogLevel()),
                field(SchedulerService.SCHEDULE_TIME_ZONE, getTimeZone() == null ? null : getTimeZone().getID()),
                field(SchedulerService.SCHEDULE_START_TIME,
                        getStartTime() == null ? null : dateUtil.formatDateTime(getStartTime(), SCHEDULER_TIME_FORMAT)),
                field(SchedulerService.SCHEDULE_END_TIME,
                        getEndTime() == null ? null : dateUtil.formatDateTime(getEndTime(), SCHEDULER_TIME_FORMAT)),
                field(SchedulerService.SCHEDULE_CONCURRENT_EXECUTION, getConcurrentExecution())));
    }

    public Boolean isEnabled() {
        return enabled;
    }
    
    public Boolean isPersisted() {
        return persisted;
    }
    
    public String getMisfirePolicy() {
        return misfirePolicy;
    }
    
    public String getScheduleType() {
        return triggerType.name();
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public Date getStartTime() {
        return startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public String getCronSchedule() {
        return cronSchedule;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public String getInvokeService() {
        return invokeService;
    }
    
    public Object getInvokeContext() {
        return invokeContext;
    }
    
    public String getInvokeLogLevel() {
        return invokeLogLevel;
    }
    
    public Boolean getConcurrentExecution() {
        return concurrentExecution;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPersisted(Boolean persisted) {
        this.persisted = persisted;
    }

    public void setInvokeService(String invokeService) {
        this.invokeService = invokeService;
    }

    public void setInvokeContext(Object invokeContext) {
        this.invokeContext = invokeContext;
    }

    public void setConcurrentExecution(Boolean concurrentExecution) {
        this.concurrentExecution = concurrentExecution;
    }
}
