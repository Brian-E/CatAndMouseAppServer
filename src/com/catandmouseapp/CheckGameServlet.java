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

public class CheckGameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CheckGameServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String gameName = req.getParameter(Constants.PARM_GAME_NAME);
		
		if (gameName==null || gameName.isEmpty()) {
			log.log(Level.WARNING, "missing parms");
			resp.sendError(Constants.ERR_MISSING_PARMS);
			return;
		}
		
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(Game.class);
			query.setFilter(Game.FIELD_GAME_NAME+"=='"+gameName+"'");
			List<Game> gameList = (List<Game>) query.execute();
			if (gameList.size()>0) {
				// oops, game of this name already exists
				log.log(Level.INFO, "Game name "+gameName+" exists");
				resp.sendError(Constants.ERR_CREATE_GAME_NAME_EXISTS);
				return;
			}
			
		}
		catch (JDOObjectNotFoundException e) {
			// This is good, just allow it to return
			log.log(Level.INFO, "Game name "+gameName+" does not exist");
		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}

}
