package com.liusm.service.model;

import java.io.Serializable;

public class Player  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 158043544712520619L;
	//OpenId
	private final String OpenId;
	//参与游戏房间号
	private int gameId = 0; 
	
	public Player(String OpenId){
		this.OpenId = OpenId;
	}
	
	public Player(String OpenId, int id){
		this.OpenId = OpenId;
		gameId = id;
	}
	
	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public String getOpenId() {
		return OpenId;
	}

	@Override
	public boolean equals(Object p){
		if(p instanceof Player){
			return ((Player) p).OpenId==OpenId;
		}else{
			return false;
		}
	}
}
