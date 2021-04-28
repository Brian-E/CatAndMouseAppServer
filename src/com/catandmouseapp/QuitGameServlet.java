package com.catandmouseapp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QuitGameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(QuitGameServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String gameNumber = req.getParameter(Constants.PARM_GAME_NUMBER);
		
		if ((playerId==null || playerId.isEmpty()) ||
				(gameNumber==null || gameNumber.isEmpty())) {
			resp.sendError(Constants.ERR_BAD_PARMS); // bad parms
			return;
		}
		
		int iGameNumber;
		try {
			iGameNumber = Integer.parseInt(gameNumber);
		}
		catch (NumberFormatException nfe) {
			log.log(Level.SEVERE, "Bad game number: "+gameNumber);
			resp.sendError(Constants.ERR_INVALID_GAME_NUMBER);
			return;
		}
		
		// Got playerId and game number, delete GamePlayer
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(GamePlayer.class);
			query.setFilter(Player.FIELD_PLAYER_ID+"=='"+playerId+"' && "+Game.FIELD_GAME_NUMBER+"=="+gameNumber);
			long lDeletedGamePlayers = query.deletePersistentAll();
			log.log(Level.INFO, "Deleted "+lDeletedGamePlayers+" Game Players");
		}		
		finally {
			if (pm!=null)
				pm.close();
		}
		
		// Call the AssignCatServlet for this game
		CMUtil.assignCat(iGameNumber);		
	}
	

}
