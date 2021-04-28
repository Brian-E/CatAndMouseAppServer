package com.catandmouseapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class GetGamesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(GetGamesServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Return the list of games to the user
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID); // List of games that a friend is in
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			List<Game> gamesList = new ArrayList<Game>();
			if (playerId!=null && !playerId.isEmpty()) {
				Query query = pm.newQuery(GamePlayer.class);
				query.setFilter(Player.FIELD_PLAYER_ID+"=='"+playerId+"'");
				List<GamePlayer> gamePlayers = (List<GamePlayer>) query.execute();
				if (gamePlayers.size()>0) {
					// build an int array of games he's in
					int[] iGames = new int[gamePlayers.size()];
					for (int i=0; i < gamePlayers.size(); i++) {
						iGames[i] = gamePlayers.get(i).getGameNumber();
					}
					
					// Get all these games
					query = pm.newQuery(Game.class);
					query.setFilter(Game.FIELD_GAME_NUMBER+"== game_param");
					query.setOrdering(Game.FIELD_GAME_NUMBER+" asc");
					query.declareParameters("int game_param");
					gamesList = (List<Game>) query.executeWithArray(iGames);
				}
			}
			else {
				Query query = pm.newQuery(Game.class);
				query.setOrdering(Game.FIELD_GAME_NUMBER+" asc");
				gamesList = (List<Game>) query.execute();
			}
			
			// return games
			List<GameBean> gbList = new ArrayList<GameBean>();
			for (int i=0; i < gamesList.size(); i++) {
				Game game = gamesList.get(i);
				GameBean gb = new GameBean(game.getGameNumber(), game.getGameName(), game.getGameType(),
						game.isPrivate(), "", game.getLocationName(), game.getLatitude(), game.getLongitude(),
						game.getRange(), game.getRatio(), game.getStartTime(), game.getEndTime());
				gb.setPlayerCount(CMUtil.getPlayerCount(gb.getGameNumber()));
				gbList.add(gb);
			}
			
			
			String json = new Gson().toJson(gbList);
		    System.out.println("SERIALIZED >> " + json);

		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");
		    resp.getWriter().write(json);		
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}

}
