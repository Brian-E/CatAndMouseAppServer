package com.catandmouseapp;

import java.io.IOException;
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

public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(LogoutServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String playerId = req.getParameter("playerId");
		if (playerId==null || playerId.isEmpty() || playerId.equals("*")) {
			log.log(Level.WARNING, "logout attempt without playerid parameter or bad playerid: "+(playerId!=null && !playerId.isEmpty() ? playerId : ""));
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, Integer.toString(Constants.ERR_MISSING_PARMS)); // no player id or bad player id
			return;
		}
		
		// If this player is in any games, delete em
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// delete em from the Player db
			Player player = pm.getObjectById(Player.class, playerId);
			pm.deletePersistent(player);

			// Delete each instance of GamePlayer, and call assignCat for each game
			Query query = pm.newQuery(GamePlayer.class);
			query.setFilter(Player.FIELD_PLAYER_ID+"=='"+playerId+"'");
			List<GamePlayer> gamePlayerList = (List<GamePlayer>) query.execute();
			int[] iGames = new int[gamePlayerList.size()];
			for (int i=0; i < gamePlayerList.size(); i++) {
				iGames[i] = gamePlayerList.get(i).getGameNumber();
			}
			Long lDeletedGamePlayers = (long) gamePlayerList.size(); 
			pm.deletePersistentAll(gamePlayerList);
			log.log(Level.INFO, "Deleted "+lDeletedGamePlayers+" from GamePlayer db");
			
			// re-assign cats
			for (int i=0; i < iGames.length; i++) {
				CMUtil.assignCat(iGames[i]);
			}
			
		}
		catch (JDOObjectNotFoundException e) {
			// player is null!
			log.log(Level.WARNING, "logout for playerid "+playerId+" failed because player is already logged out");
		}
		finally {
			if (pm!=null)
				pm.close();
		}
		
	}

}
