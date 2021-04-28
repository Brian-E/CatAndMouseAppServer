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

public class PlayerLocationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(AssignCatServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String gameNumber = req.getParameter(Constants.PARM_GAME_NUMBER);
		String centerLatitude = req.getParameter(Constants.PARM_CENTER_LATITUDE);
		String centerLongitude = req.getParameter(Constants.PARM_CENTER_LONGITUDE);
		String cornerLatitude = req.getParameter(Constants.PARM_CORNER_LATITUDE);
		String cornerLongitude = req.getParameter(Constants.PARM_CORNER_LONGITUDE);
		
		if ((gameNumber==null || gameNumber.isEmpty()) ||
				(centerLatitude==null || centerLatitude.isEmpty()) ||
				(centerLongitude==null || centerLongitude.isEmpty()) ||
				(cornerLatitude==null || cornerLatitude.isEmpty()) ||
				(cornerLongitude==null || cornerLongitude.isEmpty())) {
			log.log(Level.WARNING, "player locations request with missing fields");
			resp.sendError(Constants.ERR_MISSING_PARMS); // missing fields
			return;
		}
		
		Double dCenterLatitude = 0.0;
		Double dCenterLongitude = 0.0;
		Double dCornerLatitude = 0.0;
		Double dCornerLongitude = 0.0;
		int iGameNumber = 0;
		
		try {
			iGameNumber = Integer.parseInt(gameNumber);
			dCenterLatitude = Double.parseDouble(centerLatitude);
			dCenterLongitude = Double.parseDouble(centerLongitude);
			dCornerLatitude = Double.parseDouble(cornerLatitude);
			dCornerLongitude = Double.parseDouble(cornerLongitude);
		}
		catch (NumberFormatException e) {
			log.log(Level.WARNING, "player locations request with bad fields");
			resp.sendError(Constants.ERR_BAD_PARMS); // missing fields
			return;
		}
		
		double distance = CMUtil.distFrom(dCenterLatitude, dCenterLongitude, dCornerLatitude, dCornerLongitude);
		log.info("map distance="+distance);
		
		List<PlayerBean> playerList = new ArrayList<PlayerBean>();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(GamePlayer.class);
			query.setFilter(Game.FIELD_GAME_NUMBER+"=="+iGameNumber);
			
			List<GamePlayer> gamePlayerList = (List<GamePlayer>) query.execute();
			
			// Now get all the player objects
			for (int i=0; i < gamePlayerList.size(); i++) {
				// Get the player
				GamePlayer gamePlayer = gamePlayerList.get(i);
				try {
					Player player = pm.getObjectById(Player.class, gamePlayer.getPlayerId());
					
					// is this player within the distance we care about?
					double playerDistance = CMUtil.distFrom(dCenterLatitude, dCenterLongitude, player.getLocation().getLatitude(), player.getLocation().getLongitude());
					log.info("Player distance="+playerDistance);
					if ( playerDistance <= distance) {
						// make a player bean and add em in
						PlayerBean bean = new PlayerBean(player.getPlayerId(), player.getPlayerName(), gamePlayer.isCat(),
														 player.getLocation().getLatitude(), player.getLocation().getLongitude());
						playerList.add(bean);
					}
				}
				catch (JDOObjectNotFoundException e) {
					// whatever
				}
			}
			log.info("Returning "+playerList.size()+" players");
			
			String json = new Gson().toJson(playerList);
		    System.out.println("SERIALIZED >> " + json);

		    resp.setContentType("application/json");
		    resp.setCharacterEncoding("UTF-8");
		    resp.getWriter().write(json);				
		}
		catch (JDOObjectNotFoundException e) {
			// No game Player doesn't exist
			log.info("Couldn't find players in game: "+gameNumber);
		}
		finally {
			if (pm!=null)
				pm.close();
		}
		
	}
	
	

}
