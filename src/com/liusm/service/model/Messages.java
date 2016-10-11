package com.liusm.service.model;

import java.util.logging.Level;
import java.util.logging.Logger;

//提供本应用的各种信息
public class Messages {
	private static Logger logger = Logger. getLogger("Messages");
	public static String replace(String old,String param_name,String param_value){
		return old==null?null:old.replaceAll("[_][|]"+param_name+"[|][_]", param_value);
	}
	private static final String SUBSCRIBETXT = "感谢您的关注！\n角色功能即将上线，敬请期待！";
	public static String getSubscribeMsg(){
		return SUBSCRIBETXT;//+"\n"+START;
	}
	private static final String INVALID = "请输入\n5-10：按游戏人数创建游戏\n房间号：加入小伙伴们\n0：查看当前游戏状态";
	public static String getInvalidMsg(){
		return INVALID;
	}
	private static final String New = "_|3|_房间号：_|4|_\n身份：_|5|_\n配置：_|6|_狼人_|7|_村民\n行动人数：\n_|8|_\n_|9|_";
	public static String getCreatingMsg(Game g,String OpenId,boolean isCreator){
		StringBuffer sb = new StringBuffer();
		int pCode = g.getPlayIdMap().get(OpenId);
		int num = g.getNumOfPlayers();
		boolean[] isWolves = g.getIsWolf();
		boolean isWolf = isWolves[pCode-1];
		String identity;
		if(isWolf){
			if(num<7){
				identity = "狼人";
			}else{
logger.info("HeadWolfId"+g.getHeadWolfId()+" pCode"+pCode);
				if(g.getHeadWolfId()==(pCode-1)){
					identity = "狼王";
				}else{
					identity = "狼人";
				}
			}//TODO 红蓝狼人
		}else{
			identity = "村民";
		}
		String s = New;
		//s = replace(s,"1",isCreator?"创建":"加入");
		//s = replace(s,"2",""+num);
		s = replace(s,"3",isCreator?"创建成功，请将房间号告知小伙伴们\n\n":"");
		s = replace(s,"4",""+g.getID());//房间号
		s = replace(s,"5",identity);//身份
		//TODO 角色
		s = replace(s,"6",""+Game.NUM_OF_WOLVES[num]);//狼人数
		s = replace(s,"7",""+(num - Game.NUM_OF_WOLVES[num]));//村民数
		sb.append(Game.NUM_OF_ACTIONS[num][1]);
		sb.append(" ");
		sb.append(Game.NUM_OF_ACTIONS[num][2]);
		sb.append(" ");
		sb.append(Game.NUM_OF_ACTIONS[num][3]);
		sb.append(" ");
		sb.append(Game.NUM_OF_ACTIONS[num][4]);
		if(num>=7){sb.append("*");}
		sb.append(" ");
		sb.append(Game.NUM_OF_ACTIONS[num][5]);
		s = replace(s,"8",sb.toString());//行动人数
		s = replace(s,"9",(g.getHeadId()==(pCode-1))?"您是首任村长\n":"");
		
		s = s + "\n" + getToActionMsg(g);

		return s;
	}
	//private static final String START = "正在创建狼人之夜游戏，请输入游戏人数（5-10）或已有房间号";
	//public static String getStartMsg(){
	//	return START;
	//}
	//private static final String INTRO = "";
	private static final String TO_ACTION1 = "第";
	private static final String TO_ACTION2 = "轮行动需要";
	private static final String TO_ACTION3 = "人。\n请由行动参与人员回复“成”或“败”提交行动牌。（村民输入成）\n待行动人都提交后请输入0查看游戏状态。";
	private static final String DO_ACTION = "您已提交行动牌。\n待行动人都提交后请输入0查看游戏状态。";
	private static final String ALL_DO_ACTION = "均已提交行动牌。\n小伙伴们可输入0查看游戏状态。\n";
	private static final String RESULT1 = "第";
	private static final String RESULT2 = "轮行动结果：";
	private static final String RESULT3 = "成功 ";
	private static final String RESULT4 = "失败。\n行动";
	private static final String RESULT5 = "成功，";
	private static final String RESULT6 = "失败，";
	private static final String RESULT7 = "炸毁";
	private static final String RESULT8 = "保住";
	private static final String RESULT9 = "狼堡\n村民vs狼人: ";
	private static final String RESULT10 = "村民获胜";
	private static final String RESULT11 = "狼人获胜";
	private static final String INVALID_GAME = "输入的游戏号不存在。\n";
	private static final String NO_GAME = "您未参与任何游戏。\n";
	
/*	private static final String Created0 = "已加入房间号为";
	private static final String Created1 = "已创建房间号为";
	private static final String Created2 = "的";
	private static final String Created3 = "人游戏。\n有";
	private static final String Created4 = "个狼人，";
	private static final String Created5 = "个村民。\n行动人数为";
	private static final String Created6 = "\n您是";
	private static final String Created7 = "号玩家.";
	private static final String Created8 = "。";
	private static final String Created9 = "小伙伴 ";
	private static final String Created10 = "号是狼人。\n";
	private static final String Created11 = "\n您是第一位村长。\n";
	private static final String Created12 = "待小伙伴都加入后请输入0查看游戏状态。";
	private static final String ALL_JOIN_GAME = "\n小伙伴们到齐啦。呼唤大家输入0确认身份。";
	private static final String VILLAGER = "大地披上夜幕，沉沉的睡去。\n您的身份是村民。\n";
	private static final String WOLF = "月黑风高杀人夜。\n您的身份是狼人。\n";
	private static final String HEAD_WOLF = "月明湮灭星辰，狼嚎震颤大地，\n您的身份是大狼。\n";
	private static final String LITTLE_WOLF = "明月在指引，头狼在召唤，\n您的身份是小狼。\n";*/
	public static String getCreatedMsg(Game g,String OpenId,boolean isCreator){
		return getCreatingMsg(g,OpenId,isCreator)/*+"\n"+Created12*/;
	}
/*	public static String getAllJoinGameMsg(Game g,String OpenId){
		return getCreatingMsg(g,OpenId,false) + ALL_JOIN_GAME;
	}*/
	public static String getToActionMsg(Game g){
		return TO_ACTION1 + g.getNumOfAction() + TO_ACTION2 + Game.NUM_OF_ACTIONS[g.getNumOfPlayers()][g.getNumOfAction()] + TO_ACTION3;
	}
	public static String getDoActionMsg(){
		return DO_ACTION;
	}
	public static String getAllDoActionMsg(Game g,String OpenId){
		return ALL_DO_ACTION+getResultMsg(g,OpenId);
	}
	public static String getResultMsg(Game g,String OpenId){
		int na = g.getNumOfAction();
		StringBuffer sb = new StringBuffer();
		if(na==1){
/*			if(g.getCountOfPlayers()==g.getNumOfPlayers()){//人员到齐 夜黑 身份信息确认
				int num = g.getNumOfPlayers();
				int pCode = g.getPlayIdMap().get(OpenId);
				boolean[] isWolves = g.getIsWolf();
				boolean isWolf = isWolves[pCode-1];
				if(isWolf){
					if(num<7){
						sb.append(WOLF);
						sb.append(Created9);
						for(int i=0;i<isWolves.length;i++){
							if(isWolves[i] && (i+1)!=pCode){
								sb.append(i+1);
								sb.append(" ");
							}
						}
						sb.append(Created10);
					}else{
						if(g.getHeadWolfId()==(pCode-1)){
							sb.append(HEAD_WOLF);
							sb.append(Created9);
							for(int i=0;i<isWolves.length;i++){
								if(isWolves[i] && (i+1)!=pCode){
									sb.append(i+1);
									sb.append(" ");
								}
							}
							sb.append(Created10);
						}else{
							sb.append(LITTLE_WOLF);
						}
					}
				}else{
					sb.append(VILLAGER);
				}
				sb.append(getToActionMsg(g));
			}else{
				sb.append(Created12);
			}*/
			sb.append(getToActionMsg(g));
		}else{
			sb.append(RESULT1);
			sb.append(na-1);
			sb.append(RESULT2);
			sb.append(g.getLastNumOfActionSuccess());
			sb.append(RESULT3);
			sb.append(g.getLastNumOfActionFailed());
			sb.append(RESULT4);
			if(g.isLastIsSuccess()){
				sb.append(RESULT5);
				sb.append(RESULT7);
			}else{
				sb.append(RESULT6);
				sb.append(RESULT8);
			}
			sb.append(RESULT9);
			sb.append(g.getVillagerWin());
			sb.append(":");
			sb.append(g.getWolfWin());
			sb.append("\n");
			if(g.isOver()){
				if(g.getVillagerWin()==3){
					sb.append(RESULT10);
				}else{
					sb.append(RESULT11);
				}
			}else{
				sb.append(getToActionMsg(g));
			}
		}
		return sb.toString();
	}
	
	public static String getInvalidGameMsg(){
		return INVALID_GAME + getInvalidMsg();
	}
	
	public static String getNoGameMsg(){
		return NO_GAME + getInvalidMsg();
	}
	
	public static void main(String[] args){
		Game g = new Game(6,"nana");
		Messages.getCreatedMsg(g,"nana", true);
	}
}
