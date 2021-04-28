package com.catandmouseapp;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotificationCleanupTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(NotificationCleanupTaskServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Clean up old notifications
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(Notification.class);
			query.setFilter(Notification.FIELD_ACTIVITY_DATE+" < time_param");
			query.declareParameters("long time_param");//currentDate.getTime()-Constants.PLAYER_INACTIVITY_MILLISECONDS);
			long deletedNotifications = query.deletePersistentAll(System.currentTimeMillis()-Constants.TIME_MILLISECONDS_5_MIN);
			
			log.log(Level.INFO, "Deleted "+deletedNotifications+" old notifications");
		}
		finally {
			if (pm!=null)
				pm.close();
		}
		
	}

}
