package com.Config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.Scheduler.RunDataJob;

@WebListener
public class StartApp implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		try {
			JobKey runDataJob = new JobKey("RunDataJob", "group1");
			JobDetail job = JobBuilder.newJob(RunDataJob.class).withIdentity(runDataJob).build();

			Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName1", "group1")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/30 * * * * ?")).build();

			Scheduler scheduler;

			scheduler = new StdSchedulerFactory().getScheduler();
			//scheduler.start();
			//scheduler.scheduleJob(job, trigger1);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}

	public void contextDestroyed(ServletContextEvent event) {
		// Do stuff during webapp shutdown.
	}

}