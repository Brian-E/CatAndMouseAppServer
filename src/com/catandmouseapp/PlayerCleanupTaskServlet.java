package com.catandmouseapp;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;



public class PlayerCleanupTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(PlayerCleanupTaskServlet.class.getName());
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Any player with no activity for 5 minutes or more gets logged out and removed from all games

		// Get all players
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(Player.class);
			List<Player> players = (List<Player>) query.execute();
			for (int i=0; i < players.size(); i++) {
				Player player = players.get(i);
				// Check their activity time
				if (System.currentTimeMillis() - player.getLastActiveDate() > Constants.TIME_MILLISECONDS_5_MIN*3) {
					// We got a dangler, flush 'em. Call the logout servlet
					log.log(Level.INFO, "Logging out player "+player.getPlayerName()+" due to inactivity");
					Queue queue = QueueFactory.getDefaultQueue();
					TaskOptions to = TaskOptions.Builder.withUrl("/logout"/*+Constants.PARM_PLAYER_ID+"="+player.getPlayerId()*/).method(Method.POST);
					to.param(Constants.PARM_PLAYER_ID, player.getPlayerId());
					queue.add(to);
					
					// Send a player notification
					Notification n = new Notification(player.getPlayerId(), 0, 0, "","","", "", 
							Constants.NOTIFICATION_INACTIVTY_LOGOUT, System.currentTimeMillis());
					pm.makePersistent(n);
				}
			}
		}
		finally {
			if (pm!=null)
				pm.close();
		}
		
	}

}
