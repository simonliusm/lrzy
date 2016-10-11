package com.liusm.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeMemcachedClient;
import com.liusm.message.resp.TextMessage;
import com.liusm.service.model.Game;
import com.liusm.service.model.Messages;
import com.liusm.service.model.Player;
import com.liusm.util.MessageUtil;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 核心服务类
 * 
 * @date 2013-05-20
 */
public class CoreService implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 212174125346813001L;

	private static Logger logger = Logger. getLogger("CoreService");
	private static BaeMemcachedClient bmc = BaeFactory.getBaeMemcachedClient();
	public static final String GAME_STRING = "GAME";
	public static final String PLAYER_STRING = "PLAYER";
  
	private static void setGame(Game g){
		bmc.set(GAME_STRING+g.getID(), g);
	}
	private static Game getGame(int i){
		return (Game)bmc.get(GAME_STRING+i);
	}
	private static boolean hasGame(int i){
		return bmc.keyExists(GAME_STRING+i);
	}
	private static void setPlayer(String OpenId, int i){
		bmc.set(PLAYER_STRING+OpenId, new Player(OpenId, i));
	}
	private static Player getPlayer(String OpenId){
		return (Player)bmc.get(PLAYER_STRING+OpenId);
	}
	private static boolean deletePlayer(String OpenId){
		return bmc.delete(PLAYER_STRING+OpenId);
	}
	public static boolean deleteGame(int i){
		return bmc.delete(GAME_STRING+i);
	}
	
	/**
	 * 处理微信发来的请求
	 * 
	 * @param request
	 * @return
	 */
	public static String processRequest(HttpServletRequest request) {
		String respMessage = null;
		try {

			// 默认返回的文本消息内容
			String respContent = "请求处理异常，请稍候尝试！";

			// xml请求解析
			Map<String, String> requestMap = MessageUtil.parseXml(request);

			// 发送方帐号（open_id）
			String fromUserName = requestMap.get("FromUserName");
			// 公众帐号
			String toUserName = requestMap.get("ToUserName");
			// 消息类型
			String msgType = requestMap.get("MsgType");

			// 回复文本消息
			TextMessage textMessage = new TextMessage();
			textMessage.setToUserName(fromUserName);
			textMessage.setFromUserName(toUserName);
			textMessage.setCreateTime(new Date().getTime());
			textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
			textMessage.setFuncFlag(0);

			// 文本消息
			if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
				String content = requestMap.get("Content");
				String pswd = "狼人小分队";
				if(content!=null/*&&content.startsWith(pswd)*/){
					//内测密码
					//content = content.substring(pswd.length());

                  if(content.equals("成") || content.equals("败")){
						boolean isSuccess = content.equals("成");
						Player p = getPlayer(fromUserName);
						if(p==null){
							respContent = Messages.getInvalidMsg();
						}else{
							if(!hasGame(p.getGameId())){
								respContent = Messages.getInvalidGameMsg();
							}else{
								synchronized(getGame(p.getGameId())){
									Game g = getGame(p.getGameId());
									if(g.submitAction(fromUserName, isSuccess)){
										g.settleAction();
										respContent = Messages.getAllDoActionMsg(g,fromUserName);
									}else{
										respContent = Messages.getDoActionMsg();
									}
									setGame(g);
								}
							}
						}
					}else{
						int c=-1;
logger.log(Level.INFO,content);
						try{
							c = Integer.parseInt(content);
						}catch(NumberFormatException e){
							respContent = Messages.getInvalidMsg();
						}
						if(c==0){//刷新游戏状态
							Player p = null;
logger.log(Level.INFO,"aaa0");
							p = getPlayer(fromUserName);
							logger.log(Level.INFO,"aaa1 " + fromUserName);
							if(p==null){
								logger.log(Level.INFO,"aaa2");
								respContent = Messages.getNoGameMsg();
							}else{
								logger.log(Level.INFO,"aaa3 "+p.getGameId());
								Game g = getGame(p.getGameId());
								logger.log(Level.INFO,"aaa33 "+g.getNumOfAction());
								if(g==null){
									logger.log(Level.INFO,"aaa4");
									respContent = Messages.getNoGameMsg();
								}else{
									logger.log(Level.INFO,"aaa5");
									respContent = Messages.getResultMsg(g,fromUserName);
								}
							}
						}else if(/*c==2 || */(c >= 5 && c <= 10)){//5-10创建新游戏
							Game g = createGame(c,fromUserName);
							respContent = Messages.getCreatedMsg(g,fromUserName, true);
						}else{//房间号 加入游戏
							if(!hasGame(c)){
								respContent = Messages.getInvalidGameMsg();
							}else{
								synchronized(getGame(c)){
									Game g = getGame(c);
									Player p = getPlayer(fromUserName);
									if(p!=null && g.getID() == p.getGameId()){//避免重复加入
										respContent = Messages.getResultMsg(g,fromUserName);
									}else{
										if(joinGame(g,fromUserName)){//最后加入游戏者
											//respContent = Messages.getAllJoinGameMsg(g,fromUserName);
											respContent = Messages.getCreatedMsg(g,fromUserName, false);
										}else{//加入游戏者
											respContent = Messages.getCreatedMsg(g,fromUserName, false);
										}
									}
								}
							}
						}
					}

				}else{
					respContent = Messages.getSubscribeMsg();
				}
			}/*
			// 图片消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
				respContent = "您发送的是图片消息！";
			}
			// 地理位置消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
				respContent = "您发送的是地理位置消息！";
			}
			// 链接消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
				respContent = "您发送的是链接消息！";
			}
			// 音频消息
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
				respContent = "您发送的是音频消息！";
			}*/
			// 事件推送
			else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
				// 事件类型
				String eventType = requestMap.get("Event");
				// 订阅
				if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
					// TODO 优化改为图文消息
					respContent = Messages.getSubscribeMsg();
				}
				// 取消订阅
				else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
					// 取消订阅后用户再收不到公众号发送的消息，因此不需要回复消息
					deletePlayer(fromUserName);
				}
				// 自定义菜单点击事件
				else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
					// 自定义菜单权没有开放，暂不处理该类消息
				}
			}
			
			
			textMessage.setContent(respContent);
			respMessage = MessageUtil.textMessageToXml(textMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return respMessage;
	}
	
	public static Game createGame(int numOfPlayers,String OpenId){
		Game g = new Game(numOfPlayers,OpenId);
		logger.log(Level.INFO,"aaa6");
      	setGame(g);
      	setPlayer(OpenId,g.getID());
      	return g;
	}
	public static boolean joinGame(Game g,String OpenId){
		setPlayer(OpenId, g.getID());
		boolean b = g.playerRegister(OpenId);
		setGame(g);
		return b;
	}
}
