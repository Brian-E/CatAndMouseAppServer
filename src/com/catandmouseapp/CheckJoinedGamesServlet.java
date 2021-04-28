package com.catandmouseapp;

import java.io.IOException;
import java.util.ArrayList;
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

import com.google.gson.Gson;

public class CheckJoinedGamesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CheckJoinedGamesServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		
		if (playerId==null || playerId.isEmpty()) {
			log.log(Level.WARNING, "missing parms");
			resp.sendError(Constants.ERR_MISSING_PARMS);
			return;
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		List<GamePlayerBean> listGamePlayers = new ArrayList<GamePlayerBean>();
		try {
			Player existingPlayer = pm.getObjectById(Player.class, playerId);
			existingPlayer.setLastActiveDate(System.currentTimeMillis());
			pm.makePersistent(existingPlayer);
		}
		catch (JDOObjectNotFoundException e) {
			log.log(Level.INFO, "Player not logged in, interesting");
		}
		
		try {
			Query query = pm.newQuery(GamePlayer.class);
			query.setFilter(Player.FIELD_PLAYER_ID+"=='"+playerId+"'");
			List<GamePlayer> gamePlayerList = (List<GamePlayer>) query.execute();
			for (GamePlayer gamePlayer : gamePlayerList) {
				listGamePlayers.add(new GamePlayerBean(gamePlayer.getPlayerId(), gamePlayer.getGameNumber(), gamePlayer.isCat()));
			}			
		}
		catch (JDOObjectNotFoundException e) {
			// This is good, just allow it to return
			log.log(Level.INFO, "Player not found in any games");
		}
		finally {
			String json = new Gson().toJson(listGamePlayers);
		    System.out.println("SERIALIZED >> " + json);

		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");
		    resp.getWriter().write(json);		
			if (pm!=null)
				pm.close();
		}
	}

}
