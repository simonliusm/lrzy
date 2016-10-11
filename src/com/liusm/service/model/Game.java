package com.liusm.service.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.baidu.bae.api.factory.BaeFactory;
import com.baidu.bae.api.memcache.BaeMemcachedClient;
import com.baidu.bae.api.util.BaeEnv;
import com.liusm.service.CoreService;

public class Game implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6604731555823738366L;
	private static Logger logger = Logger. getLogger("CoreService");
  	//不同游戏人数下的狼人个数
	public static final int[] NUM_OF_WOLVES = {0,0,0,0,0,2,2,3,3,3,4};
	//不同人数和狼堡编号下的行动参与人数
	public static final int[][] NUM_OF_ACTIONS = {{},{},{},{},{},
		                                        {0,2,3,2,3,3},
		                                        {0,2,3,4,3,4},
		                                        {0,2,3,3,4,4},
		                                        {0,3,4,4,5,5},
		                                        {0,3,4,4,5,5},
		                                        {0,3,4,4,5,5}};
  	//角色
	public static final String[] ALL_ROLES = {"包打听","单相思","通灵师","退休村长","马屁精","狼人爱好者","胆小鬼","心太软","中华田园犬","嘘嘘","洛丽塔","塔罗师","地头蛇","瞌睡虫","正常人","老外","耿直男","普通人","普通人","普通人","普通人","普通人"};
	//角色说明
	public static final String[] ALL_ROLES_DESC = {"如果你是村长，你可以翻转该角色牌，在打乱行动牌之前，独自查看一位玩家的行动牌。",
		"翻转该角色牌，指定除你之外的另一名玩家。村长选你时必须选他,但是选他时不一定要选你。",
		"在村长交替时，你可以翻转该角色牌，交换除你之外其他任意两位玩家的身份牌",
		"在此回合投票时，翻转该角色牌，一票抵两票。",
		"你永远支持村长的决定，不能投反对票。",
		"在行动前夜阶段后，你必须将身份牌给你指定的一位玩家单独查看。",
		"如果你是村民，你只敢出成功牌；如果你是狼人，你只敢出失败牌。",
		"如果你是狼人，并且当前回合狼人胜利两次，村民没有胜利过，则你的身份暂时变成村民。",
		"你在说话时必须用汪汪表示村民，用啊呜表示狼人。",
		"翻转该角色牌，拉当前村长去上厕所。直接进入下一回合，计一次投票失败。",
		"行动前夜阶段后，你必须查看左边或右边玩家的身份牌，从此保持沉默。",
		"当你做村长时，每次翻开行动牌前你猜哪方胜利，猜对了继续做村长。",
		"在玩家上交的行动牌被打乱且未翻开时，翻转该角色牌，弃掉一张被选中的行动牌。",
		"在攻击1号狼堡时，你不能被村长选中。",
		"在每回合开始前，你必须指着一位玩家大喊：“你是狼人！”",
		"你在说话时必须使用家乡话或英语，即使有人听不懂你在说什么。",
		"你必须正面朝上提交行动牌。",
		"你是没有特殊能力的普通人。",
		"你是没有特殊能力的普通人。",
		"你是没有特殊能力的普通人。",
		"你是没有特殊能力的普通人。",
		"你是没有特殊能力的普通人。"};
	//房间号
	private final int ID;
	//游戏人数
	private final int numOfPlayers;
	//身份 从0开始编号
	private boolean[] isWolf;
	//大狼
	private int headWolfId;
	//初始村长
	private int headId;
	//玩家list
	private String[] players;
	private Map<String,Integer> playIdMap = new HashMap<String,Integer>();//从1开始编号
	//已参与玩家个数
	private int countOfPlayers = 0;
	//当前行动局数
	private int numOfAction = 1;
	public int getNumOfAction() {
		return numOfAction;
	}

	//狼人获胜数 村民获胜数
	private int wolfWin = 0;
	private int villagerWin = 0;
	//游戏结束标志
	private boolean isOver = false;
	//已提交行动牌人数 已提交成功行动数 已提交失败行动数
	private int numOfActionSubmit = 0;
	private int numOfActionSuccess = 0;
	private int numOfActionFailed = 0;
	//上次行动情况
	private boolean lastIsSuccess;
	private int lastNumOfActionSuccess;
	private int lastNumOfActionFailed;
	
	public Map<String, Integer> getPlayIdMap() {
		return playIdMap;
	}

	public boolean[] getIsWolf() {
		return isWolf;
	}

	public void setPlayIdMap(Map<String, Integer> playIdMap) {
		this.playIdMap = playIdMap;
	}

	public Game(int numOfPlayers){
		//游戏计数
		long countOfGames = 0;
		BaeMemcachedClient a = BaeFactory.getBaeMemcachedClient();
		//创建游戏基本参数 随机挑选狼人、大狼和初始村长
		this.numOfPlayers = numOfPlayers;
		isWolf = new boolean[numOfPlayers];
		Random random = new Random();
		int headWolfNum = random.nextInt(NUM_OF_WOLVES[numOfPlayers]);//TODO 红蓝狼人
		for(int i=0;i<NUM_OF_WOLVES[numOfPlayers];i++){
			int pCode;
			do{
				pCode = random.nextInt(numOfPlayers);
			}while(isWolf[pCode]);
			isWolf[pCode] = true;
			if(i==headWolfNum){
				headWolfId = pCode;
			}
		}
		headId = random.nextInt(numOfPlayers);
		logger.info("headWolfId"+headWolfId);
		
		countOfGames = a.addOrIncr("countOfGames");
		countOfGames = a.incr("countOfGames");
		logger.info("gamecountOfGames"+countOfGames);
		//创建游戏id房间号
		long id = 100 + countOfGames;
		if(id>=10000){
			id=100;
			for(int ii=100;ii<5000;ii++){
				CoreService.deleteGame(ii);
			}
		}else if(id==5000){
			for(int ii=5000;ii<10000;ii++){
				CoreService.deleteGame(ii);
			}
		}
		ID = (int)id;
		
		players = new String[numOfPlayers];
		
		logger.log(Level.INFO, countOfGames+"盘游戏开始");
	}

	public int getHeadWolfId() {
		return headWolfId;
	}

	public int getID() {
		return ID;
	}

	public boolean isOver() {
		return isOver;
	}

	public Game(int numOfPlayers, String OpenId){
		this(numOfPlayers);
		playerRegister(OpenId);
	}
	//玩家注册 返回true表示游戏人数已满 游戏开始
	public boolean playerRegister(String OpenId){
		players[(int) countOfPlayers++] = OpenId;
		playIdMap.put(OpenId, countOfPlayers);
		if(countOfPlayers == numOfPlayers){
			return true;
		}else{
			return false;
		}
	}
	
	//行动人记录，避免重复提交
	private String[][] actionPerson = new String[5][5];
	//提交行动牌 返回true表示行动牌提交足够 行动执行
	public boolean submitAction(String OpenId,boolean isSuccess){
		//重复提交校验
		for(String s : actionPerson[numOfAction-1]){
			if(OpenId.equals(s)){//本轮已提交过
				return false;
			}
		}
		//记录
		actionPerson[numOfAction-1][numOfActionSubmit] = OpenId;
		
		if(isSuccess){
			numOfActionSuccess++;
		}else{
			numOfActionFailed++;
		}
		numOfActionSubmit++;
		if(numOfActionSubmit == NUM_OF_ACTIONS[numOfPlayers][numOfAction]){
			return true;
		}else{
			return false;
		}
	}
	
	//结算本次行动 在submitAcion返回true时调用
	public void settleAction(){
		boolean isSuccess;//本局是否行动成功
		//普通局 有fail行动牌就fail
		int needFail = 1;
		//7人以上 第4局需要2fail才fail
		if(numOfPlayers >= 7 && numOfAction == 4){
			needFail = 2;
		}
		if(numOfActionFailed >= needFail){//本局行动失败
			isSuccess = false;
			wolfWin++;
			if(wolfWin == 3){
				isOver = true;
			}
		}else{
			isSuccess = true;
			villagerWin++;
			if(villagerWin == 3){
				isOver = true;
			}
		}
		//记录上一局状态
		lastIsSuccess = isSuccess;
		lastNumOfActionSuccess = numOfActionSuccess;
		lastNumOfActionFailed = numOfActionFailed;
		
		//局数+1
		numOfAction++;
		//重置
		numOfActionSubmit = 0;
		numOfActionSuccess = 0;
		numOfActionFailed = 0;
	}

	public int getHeadId() {
		return headId;
	}

	public int getCountOfPlayers() {
		return countOfPlayers;
	}

	public boolean isLastIsSuccess() {
		return lastIsSuccess;
	}

	public int getLastNumOfActionSuccess() {
		return lastNumOfActionSuccess;
	}

	public int getLastNumOfActionFailed() {
		return lastNumOfActionFailed;
	}

	public int getWolfWin() {
		return wolfWin;
	}

	public int getVillagerWin() {
		return villagerWin;
	}
	
	public int getNumOfPlayers() {
		return numOfPlayers;
	}

	public static void main(String[] args){
		//Game g = new Game(5,"aaaabbbb");
		int numOfPlayers = 7;
		boolean[] isWolf = new boolean[numOfPlayers];
		Random random = new Random();
		int headWolfId=100;
		int headWolfNum = random.nextInt(NUM_OF_WOLVES[numOfPlayers]);
		for(int i=0;i<NUM_OF_WOLVES[numOfPlayers];i++){
			int pCode;
			do{
				pCode = random.nextInt(numOfPlayers);
			}while(isWolf[pCode]);
			isWolf[pCode] = true;
			if(i==headWolfNum){
				headWolfId = pCode;
			}
		}
		System.out.println(headWolfNum);
		System.out.println(headWolfId);
		for(boolean b:isWolf){
			System.out.println(b);
		}
		//headId = random.nextInt(numOfPlayers);
	}
}
