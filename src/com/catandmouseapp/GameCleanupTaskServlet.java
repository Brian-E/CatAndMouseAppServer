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

public class GameCleanupTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GameCleanupTaskServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Checks for any lingering games - games that are past their end date
		
		// Get all games
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(Game.class);
			query.setFilter(Game.FIELD_END_TIME+">0");
			List<Game> games = (List<Game>) query.execute();
			for (int i=0; i < games.size(); i++) {
				Game game = games.get(i);
				// Is it still valid?
				if (System.currentTimeMillis() > game.getEndTime()) {
					// This party's over!
					log.log(Level.INFO, "Deleting game "+game.getGameName()+" because it has expired");
					
					// Send notification
					Notification n = new Notification(Constants.ALL, game.getGameNumber(), game.getGameType(), "","","", "", 
							Constants.NOTIFICATION_GAME_OVER, System.currentTimeMillis());
					pm.makePersistent(n);
					
					// Delete all players logged into the game
					query = pm.newQuery(GamePlayer.class);
					query.setFilter(Game.FIELD_GAME_NUMBER+"=="+game.getGameNumber());
					long deletedPlayers = query.deletePersistentAll();
					log.log(Level.INFO, "Deleted "+deletedPlayers+" players from game "+game.getGameName());
					
					// Delete the game
					pm.deletePersistent(game);
				}
			}
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}

}
