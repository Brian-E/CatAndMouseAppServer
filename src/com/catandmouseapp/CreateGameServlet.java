package com.catandmouseapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.GeoPt;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CreateGameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CreateGameServlet.class.getName());
	private Object lockObject = new Object();
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Get the game bean
		BufferedReader reader = req.getReader();
//		String temp;
//		String jsonGame = null;
//		while((temp = reader.readLine())!=null)
//			jsonGame += temp;
//		
//		//String jsonGame = req.getParameter(Constants.PARM_GAME_BEAN);
//		if (jsonGame==null || jsonGame.isEmpty()) {
//			log.log(Level.SEVERE, "json string sucks: "+jsonGame);
//			resp.sendError(Constants.ERR_BAD_PARMS);
//			return;
//		}
		
		GameBean gb = null;
		try {
			gb = new Gson().fromJson(reader, GameBean.class);
			if (gb==null || (gb.getGameName()==null || gb.getGameName().isEmpty())) {
				log.log(Level.SEVERE, "GameBean is null: "+reader.toString());
				resp.sendError(Constants.ERR_BAD_PARMS);
				return;
			}
		}
		catch (JsonSyntaxException e) {
			log.log(Level.SEVERE, "Couldnt convert json string to GameBean. Error: "+e.toString()+"\n jsonGame="+reader.toString());
			resp.sendError(Constants.ERR_BAD_PARMS);
			return;
		}
		
		if (gb!=null) {
			// OK, we should have decent parameters at this point. Let's validate and create a game
			PersistenceManager pm = PMF.get().getPersistenceManager();
			try {
				// First, does a game by this name already exist?
				String gameName = gb.getGameName();
				Query query = pm.newQuery(Game.class);
				query.setFilter(Game.FIELD_GAME_NAME+"=='"+gameName+"'");
				List<Game> gameList = (List<Game>) query.execute();
				if (gameList.size() > 0) {
					// Darn, game by this name already exists
					log.log(Level.WARNING, "Game by this name already exists: "+gameName);
					resp.sendError(Constants.ERR_CREATE_GAME_NAME_EXISTS);
					return;
				}
				
				// Game name ok. Everything else should be OK.  Get the next available game number
				synchronized(lockObject) {
					int iGameNumber = 1;
					query = pm.newQuery("select "+Game.FIELD_GAME_NUMBER+" from "+Game.class.getName()+" order by "+Game.FIELD_GAME_NUMBER+" desc");
					List<Integer> listHighGameNumber = (List<Integer>) query.execute();
					if (listHighGameNumber.size()==0)
						iGameNumber = 1; // Should be the highest one
					else
						iGameNumber = listHighGameNumber.get(0)+1;
					
					// Got my game number, now create the entity
					Game game = new Game(iGameNumber, gb.getGameName(), gb.getGameType(), gb.isPrivate(), gb.getPassword(), 
							gb.getLocationName(), gb.getLatitude(), gb.getLongitude(), gb.getRange(), gb.getRatio(), gb.getStartTime(), 
							gb.getEndTime());
					pm.makePersistent(game);
				}
				CMUtil.sendLeaderboardMail(gameName);
			}
			finally {
				if (pm!=null)
					pm.close();
			}
		}
		
		
	}

}
