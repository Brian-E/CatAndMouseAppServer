package com.catandmouseapp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.GeoPt;

public class UpdateLocationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(UpdateLocationServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Get parms and update location
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String latitude = req.getParameter(Constants.PARM_LATITUDE);
		String longitude = req.getParameter(Constants.PARM_LONGITUDE);
		
		if ((playerId==null || playerId.isEmpty()) || (latitude==null || latitude.isEmpty()) ||
				(longitude==null || longitude.isEmpty())) {
			log.log(Level.WARNING, "missing parms");
			resp.sendError(Constants.ERR_MISSING_PARMS);
			return;
		}
		
		float fLatitude = 0;
		float fLongitude = 0;
		try {
			fLatitude = Float.parseFloat(latitude);
			fLongitude = Float.parseFloat(longitude);
		}
		catch (NumberFormatException e) {
			log.log(Level.WARNING, "bad parms");
			resp.sendError(Constants.ERR_BAD_PARMS);
			return;
		}
		
		// Update location
		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Player player = pm.getObjectById(Player.class, playerId);
			if (player!=null) {
				player.setLocation(new GeoPt(fLatitude, fLongitude));
				player.setLastActiveDate(System.currentTimeMillis());
			}
			else {
				log.log(Level.WARNING, "Bad playerid: "+playerId);
				resp.sendError(Constants.ERR_INVALID_PLAYER_ID);
				return;
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
