package com.catandmouseapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;

import com.google.gson.Gson;

public class GetNotificationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GetNotificationsServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Query for all notifications for this game player
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String strGameNumbers = req.getParameter(Constants.PARM_GAME_NUMBER);
		String [] gameNumbers = strGameNumbers!=null && !strGameNumbers.isEmpty() ? strGameNumbers.split(",") : null; 
		String timeLastUpdate = req.getParameter(Constants.PARM_LAST_UPDATE_TIME);
		
		if ((playerId==null || playerId.isEmpty()) || (gameNumbers==null || gameNumbers.length==0) || 
				(timeLastUpdate==null || timeLastUpdate.isEmpty())) {
			log.log(Level.WARNING, "missing parms");
			resp.sendError(Constants.ERR_MISSING_PARMS);
			return;
		}
		
		long lTimeLastUpdate = 0;
		int [] iGameNumbers = new int[gameNumbers.length];
		try {
			lTimeLastUpdate = Long.parseLong(timeLastUpdate);
			for (int i=0; i < gameNumbers.length; i++) {
				iGameNumbers[i] = Integer.parseInt(gameNumbers[i]);
			}
		}
		catch (NumberFormatException e) {
			log.log(Level.WARNING, "bad parms");
			resp.sendError(Constants.ERR_BAD_PARMS);
			return;
		}
		
		// Query for my notifications
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// Update this players last activity date
			Player player = pm.getObjectById(Player.class, playerId);
			if (player!=null)
				player.setLastActiveDate(System.currentTimeMillis());

			Query query = pm.newQuery(Notification.class);
			String gameFilter=" (";
			for (int i=0; i < iGameNumbers.length; i++) {
				if (i>0)
					gameFilter+=" || ";
				gameFilter+=Game.FIELD_GAME_NUMBER+"=="+iGameNumbers[i];
			}
			gameFilter +=")";
			String filter = "("+Notification.FIELD_INTENDED_PLAYER_ID+"=='"+playerId+"'"+
			" || "+Notification.FIELD_INTENDED_PLAYER_ID+"=='"+Constants.ALL+"')"+
			" && "+Notification.FIELD_ACTIVITY_DATE+" > "+lTimeLastUpdate+
			" && "+gameFilter;
			query.setFilter(filter);
			query.setOrdering(Notification.FIELD_ACTIVITY_DATE+" asc");
			log.log(Level.INFO, "Notification filter: "+filter);
			@SuppressWarnings("unchecked")
			List<Notification> notifications = (List<Notification>) query.execute();
			if (notifications.size() > 0) {
				List<NotificationBean> beanList = new ArrayList<NotificationBean>();
				for (int i=0; i < notifications.size(); i++) {
					Notification n = notifications.get(i);
					NotificationBean bean = new NotificationBean(n.getGameNumber(), n.getGameType(),
							n.getIntendedPlayerId(), n.getCatPlayerId(), n.getCatPlayerName(), 
							n.getMousePlayerId(), n.getMousePlayerName(),
							n.getNotificationType(), n.getActivityDate());
					beanList.add(bean);
				}
				// return notifications
				String json = new Gson().toJson(beanList);
			    System.out.println("SERIALIZED >> " + json);

			    resp.setContentType("application/json");
			    resp.setCharacterEncoding("UTF-8");
			    resp.getWriter().write(json);				
			}			
		}
		catch (JDOObjectNotFoundException e) {
			log.log(Level.WARNING, "Player doesn't exist!");
			resp.sendError(Constants.ERR_BAD_PARMS);
			return;
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}
}
