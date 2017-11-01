package jp.co.jrits.yoyakutter_bot.main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletSession;

import com.ibm.watson.developer_cloud.conversation.v1.model.Entity;

import jp.co.jrits.yoyakutter_bot.database.SQLExecuter;
import jp.co.jrits.yoyakutter_bot.database.SQLExecuter.PlanResult;
import jp.co.jrits.yoyakutter_bot.database.SQLExecuter.Resource;
import jp.co.jrits.yoyakutter_bot.database.SQLExecuter.User;
import jp.co.jrits.yoyakutter_bot.watson.YoyakuConversation;
import jp.co.jrits.yoyakutter_bot.watson.YoyakuConversation.YoyakuConvEntity;

/**
 *
 * @author
 *
 */
public class ReplyMessageHandler {
	YoyakuConversation yoyakuConv;
	SQLExecuter sqlexecuter;
	Exception exception;
	private Map<String,Object> context;

    public ReplyMessageHandler() {
        yoyakuConv = new YoyakuConversation();
        try {
			sqlexecuter = new SQLExecuter();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			this.exception=e;
		}
    }

    @SuppressWarnings("unchecked")
	public void reply(SlackletRequest req ,SlackletResponse resp) throws IOException {

        String content = req.getContent();

        // セッションを取得する（セッションはユーザー毎に固有）
        SlackletSession session = req.getSession();

        context = (Map<String,Object>) session.getAttribute("context", null);

        YoyakuConvEntity nextConv = askConversation(context, content);
        context = nextConv.getContext();

		// メッセージを送信したユーザーのメンションを取得する
		String mention = req.getSender().getUserName();

		String type = (String) nextConv.getContext().get("type");

		String message = null;

		for(String key:context.keySet()) {
			System.out.println("Context:" +key +":" +context.get(key));
		}

		message = access(nextConv,mention,content);

		if (message != null) {
			resp.reply(message);
		}
        session.setAttribute("context", context);
    }

    private String access(YoyakuConvEntity nextConv,String mention,String content) {
    	String message = null;
		List<Entity> entities=nextConv.getEntities();
		String type = (String) nextConv.getContext().get("type");

		if (type != null && !type.isEmpty()) {
	        try {
	        	message = execute(type,mention,context,entities);
	        	if (message.contains("resourceId")) {
	                String[] ids = message.split(":");
	                context.put("rental", ids[1]);
	                context.put("rental_name", ids[2]);
	                context.put("type", "");
	                message = setContext(nextConv,true).getMessage();
	        	} else if (message.contains("userId")) {
	                String[] ids = message.split(":");
	                if (Integer.parseInt(ids[1]) > -1) {
	                    context.put("userid", ids[1]);
	                    context.put("type", "");
	                    nextConv = askConversation(context, content);
	                    message = access(nextConv,mention,null);
	                } else {
	                    message = "Yoyakutter にユーザ未登録です。管理者に登録を依頼してください。";
	                }
	        	} else {
	                context.put("type", "");
	                setContext(nextConv,false);
	                if (message.length() == 0 ) {
	                	message = nextConv.getMessage();
	                }
	        	}
			} catch (Exception e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
		        message = e.getMessage();
			}
		} else {
            if (setContext(nextConv)) {
                nextConv =setContxetOfConversation(context);
                context=nextConv.getContext();
            }
			message = nextConv.getMessage();
		}
    	return message;
    }

    private YoyakuConvEntity setContext(YoyakuConvEntity nextConv,boolean updateFlg) {
    	boolean flg = setContext(nextConv);
        if (flg || updateFlg) {
            nextConv =setContxetOfConversation(context);
            context=nextConv.getContext();
        }

		return nextConv;
    }

    private boolean setContext(YoyakuConvEntity nextConv) {
		List<Entity> entities=nextConv.getEntities();

        String date = (String)context.get("date");
		String dateTo = (String)context.get("dateto");
		String time = (String)context.get("time");
		String timeTo = (String)context.get("timeto");
		String category = (String)context.get("category");
		String rental = (String)context.get("rental");
		boolean updateFlg = false;

			int index = 0;
			int index2 = 0;
			for (Entity entity:entities) {
				if (entity.getEntity().equals("sys-date")) {
                    if (date.isEmpty()) {
						date=entity.getValue();
						System.out.println("Set Context:date=" + date );
						context.put("date", date);
						updateFlg = true;
                    } else if (dateTo.isEmpty() || index == 1) {
						dateTo=entity.getValue();
	          			System.out.println("Set Context:dateto=" + dateTo );
						context.put("dateto", dateTo);
						updateFlg = true;
					}

                    index++;
				} else if (entity.getEntity().equals("sys-time")) {
                    if (null == time || time.isEmpty()) {
						time=entity.getValue();
						System.out.println("Set Context:time=" + time );
						context.put("time", time);
						updateFlg = true;
                    } else if (index2 == 1 || null == timeTo || timeTo.isEmpty()) {
                    	if(!entity.getValue().equalsIgnoreCase(time)) {
    						timeTo=entity.getValue();
                  			System.out.println("Set Context:timeto=" + timeTo );
    						context.put("timeto", timeTo);
    						updateFlg = true;
                    	}
					} else {
						index2++;
					}
				} else if (entity.getEntity().equals("category")) {
					category=entity.getValue();
          			System.out.println("Set Context:category=" + category );
					context.put("category", category);
					updateFlg = true;
				}
				System.out.println("Entity:" + entity.getEntity() + entity.getValue());
		}
		String timezone = (String)context.get("timezone");
		if (null == timezone || timezone.isEmpty()) {
			context.put("timezone", "Asia/Tokyo");
		}
		return updateFlg;
    }

    private YoyakuConvEntity askConversation(Map<String,Object> context, String content) {
        YoyakuConvEntity nextConv = yoyakuConv.reply(context, content);
    	return nextConv;
    }

    private YoyakuConvEntity setContxetOfConversation(Map<String,Object> context) {
        YoyakuConvEntity nextConv = yoyakuConv.setContext(context);
    	return nextConv;
    }

    private String execute(String type,String mention,Map<String,Object> contexts,List<Entity> entities) throws Exception {
    		StringBuffer buf = new StringBuffer();
    		if (type.equals("searchPlanTable")) {
    			String category = (String) contexts.get("category");
    			String date= (String) contexts.get("date");
    			String dateto= (String) contexts.get("dateto");
    			List<String> sysDate=new ArrayList<>();
    			sysDate.add(date);
    			if (!dateto.isEmpty()) {
        			sysDate.add(dateto);
    			}
    			Collection<PlanResult> plans = sqlexecuter.selectPlanResult(category,mention,sysDate);
    			if (plans.size() > 0) {
        			for(PlanResult plan:plans) {
        		        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");

        			}
    			} else {
    		        buf.append("見つかりませんでした");
    			}
    		} else if (type.equals("selectRentalResource")) {
    			String rental = null;
    			String bihin = null;
    			String sysNumber = null;
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("category") && rental == null) {
    					rental=entity.getValue();
    				}

    				if (bihin == null && entity.getEntity().equals("bihin")) {
    					bihin=entity.getValue();
    				}

    				if (sysNumber == null && entity.getEntity().equals("sys-number")) {
    					sysNumber=entity.getValue();
    				}
    			}
    			if (null == rental || rental.isEmpty()) {
    				rental = (String)contexts.get("category");
    			}
    			Collection<PlanResult> plans = null;
    			if (null == rental || rental.isEmpty()) {
        			plans = sqlexecuter.selectRentalResource(mention);
    			} else {
        			plans = sqlexecuter.selectRentalResource(rental,mention);
    			}
    			switch (plans.size()){
				case 0:
    		        buf.append("現在借りているものは見つかりませんでした");
					break;
				case 1:
					PlanResult rs = plans.iterator().next();
    		        buf.append("resourceId:" + rs.getResourceId() +":"+rs.getResourceName());
					break;
				default:
        			for(PlanResult plan:plans) {
        		        buf.append(plan.getResourceName() + ":" + plan.getStartTime()+"\n");
        			}
    			}
    		} else if (type.equals("selectResource")) {
    			String rental = null;
    			String bihin = null;
    			String sysNumber = null;
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("category") && rental == null) {
    					rental=entity.getValue();
    				}

    				if (bihin == null && entity.getEntity().equals("bihin")) {
    					bihin=entity.getValue();
    				}

    				if (sysNumber == null && entity.getEntity().equals("sys-number")) {
    					sysNumber=entity.getValue();
    				}
    			}
    			if (null == rental || rental.isEmpty()) {
    				rental = (String)contexts.get("category");
    			}
    			Collection<Resource> resources = null;

    			if (bihin != null) {
        			resources = sqlexecuter.selectResource(bihin+sysNumber, rental);
    			} else {
        			resources = sqlexecuter.selectResource(rental);
    			}
    			switch (resources.size()){
				case 0:
    		        buf.append("見つかりませんでした");
					break;
				case 1:
					Resource rs = resources.iterator().next();
    		        buf.append("resourceId:" + rs.getId() +":"+rs.getResourceName());
					break;
				default:
					buf.append("借りるのは"+rental+"ですね。以下からお選びください。\n");
        			for(Resource resource:resources) {
        		        buf.append(resource.getResourceName() +"\n");
        			}
    			}
    		} else if (type.equals("insertPlanTable")) {
    			String rental = (String) contexts.get("rental");
    			String date= (String) contexts.get("date");
    			String dateto= (String) contexts.get("dateto");
    			String time = (String) contexts.get("time");
    			String timeto = (String) contexts.get("timeto");
    			String userid = (String) contexts.get("userid");
                String mode2 = (String) contexts.get("mode2");
    			if (null == time || time.isEmpty()) {
    				DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
    				LocalDateTime d = LocalDateTime.now();
    				time = d.format(f);
    			}
    			if (null == timeto || timeto.isEmpty()) {
    				timeto = "17:30:00";
    			}

    			String useFrom = null;
    			if (mode2.equalsIgnoreCase("rental")) {
    			    useFrom = date + " " + time;
    			}

    			sqlexecuter.insertPlanResult(rental, userid, useFrom, date + " " + time, dateto + " " + timeto);
    		} else if (type.equals("updatePlanResult")) {
    			String rental = (String) contexts.get("rental");
    			String userid = (String) contexts.get("userid");
				DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime d = LocalDateTime.now();
				String finishtime = d.format(f);
    			sqlexecuter.updatePlanResult(rental, userid, finishtime);
    		} else if (type.equals("selectUser")) {
    			Collection<User> users = null;
       			users = sqlexecuter.selectUser(mention);
    			if (users.size() == 1) {
        		    buf.append("userId:" + users.iterator().next().getId());
    			} else {
    		        buf.append("userId:-1");
    			}
    		}
    	return buf.toString();
    }
}