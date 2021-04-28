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

import com.google.appengine.api.datastore.GeoPt;

public class LoginServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(LoginServlet.class.getName());

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		// get player id, name and MAC address from the request and log em in
		// Errors for existing id, missing parameters, 
		String playerId = req.getParameter(Constants.PARM_PLAYER_ID);
		String playerName = req.getParameter(Constants.PARM_PLAYER_NAME);
		String macAddress = req.getParameter(Constants.PARM_MAC_ADDR);
		String latitude = req.getParameter(Constants.PARM_LATITUDE);
		String longitude = req.getParameter(Constants.PARM_LONGITUDE);

		if ((playerId==null || playerId.isEmpty()) || (playerName==null || playerName.isEmpty()) || 
				(macAddress==null || macAddress.isEmpty()) || (latitude==null || latitude.isEmpty()) ||
				(longitude==null || longitude.isEmpty())) {
			resp.sendError(Constants.ERR_MISSING_PARMS); // missing fields
			log.log(Level.WARNING, "login request with missing fields");
			return;
		}

		float fLatitude, fLongitude;
		try {
			fLatitude = Float.parseFloat(latitude);
		}
		catch (NumberFormatException e) {
			fLatitude = 0;
		}
		try {
			fLongitude = Float.parseFloat(longitude);
		}
		catch (NumberFormatException e) {
			fLongitude = 0;
		}

		GeoPt location = new GeoPt(fLatitude, fLongitude);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			// check for existing player
			Player existingPlayer = pm.getObjectById(Player.class, playerId);
			if (existingPlayer!=null && !existingPlayer.getMacAddress().equals(macAddress)) {
				// Player is already logged in at a different mac address
				log.log(Level.WARNING, "login request for already logged in user id="+existingPlayer.getPlayerId()+" name="+existingPlayer.getPlayerName());
				resp.sendError(Constants.ERR_LOGIN_EXISTING_DIFF_MAC_ADDR);//, existingPlayer.getMacAddress()); // player already logged in at given mac address
				return;
			}
			else if (existingPlayer!=null && existingPlayer.getMacAddress().equals(macAddress)) {
				// Same player logging in again. Oh well, update his location
				existingPlayer.setLocation(location);
			}
		}
		catch (JDOObjectNotFoundException e) {
			// Player doesn't exist, put em in
			Player newPlayer = new Player(playerId, playerName, macAddress, location);
			pm.makePersistent(newPlayer);
		}
		//			if (existingPlayer!=null && !existingPlayer.getMacAddress().equals(macAddress)) {
		//				// Player is already logged in at a different mac address
		//				log.log(Level.WARNING, "login request for already logged in user id="+existingPlayer.getPlayerId()+" name="+existingPlayer.getPlayerName());
		//				resp.sendError(Constants.ERR_LOGIN_EXISTING_DIFF_MAC_ADDR);//, existingPlayer.getMacAddress()); // player already logged in at given mac address
		//			}
		//			else if (existingPlayer!=null && existingPlayer.getMacAddress().equals(macAddress)) {
		//				// Same player logging in again. Oh well, update his location
		//				existingPlayer.setLocation(location);
		//			}
		//			else {
		//				// Player doesn't exist, put em in
		//				Player newPlayer = new Player(playerId, playerName, macAddress, location);
		//				pm.makePersistent(newPlayer);
		//			}
		//			
		//		}
		finally {
			if (pm!=null)
				pm.close();
		}
	}	

}
