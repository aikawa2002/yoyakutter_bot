package jp.co.jrits.yoyakutter_bot.main;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletSession;
import org.riversun.xternal.simpleslackapi.SlackAttachment;
import org.riversun.xternal.simpleslackapi.SlackUser;

import com.ibm.watson.developer_cloud.conversation.v1.model.Entity;

import javafx.collections.transformation.SortedList;
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

        Map<String,Object> context = (Map<String,Object>) session.getAttribute("context", null);

        YoyakuConvEntity nextConv = askConversation(context, content);
        context = setDateToContext(nextConv);

		// メッセージを送信したユーザーのメンションを取得する
		String mention = req.getUserDisp();

		List<Entity> entities=nextConv.getEntities();
		String type = (String) nextConv.getContext().get("type");

		resp.reply(nextConv.getMessage());
		if (type != null) {
            try {
            	String message = execute(type,mention,context,entities);
            	if (message.contains("resourceId")) {
                    String[] ids = message.split(":");
                    context.put("rental", ids[1]);
                    context.put("rental_name", ids[2]);
                    nextConv =setContxetOfConversation(context);
                    context = nextConv.getContext();
                    message = nextConv.getMessage();
            	}
            	resp.reply(message);
    		} catch (Exception e) {
    			// TODO 自動生成された catch ブロック
    			e.printStackTrace();
    	        resp.reply(e.getMessage());
    		}
        }
        session.setAttribute("context", context);
    }

    private Map<String,Object> setDateToContext(YoyakuConvEntity nextConv) {
		Map<String,Object> contexts=nextConv.getContext();
		List<Entity> entities=nextConv.getEntities();

		String dateTo = (String)contexts.get("dateto");
		String date = (String)contexts.get("date");
		for(String key:contexts.keySet()) {
			System.out.println("Context:" +key +":" +contexts.get(key));
		}

		if (dateTo.isEmpty()) {
			int index = 0;
			for (Entity entity:entities) {
				if (entity.getEntity().equals("sys-date")) {
					if (index == 1 || !date.isEmpty()) {
						dateTo=entity.getValue();
						contexts.put("dateto", dateTo);
						setContxetOfConversation(contexts);
					} else {
						index++;
					}
				}
				System.out.println("Entity:" + entity.getEntity() + entity.getValue());
			}
		}
		return contexts;
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
    			Collection<PlanResult> plans = sqlexecuter.selectPlanResult(category,"aic",sysDate);
    			if (plans.size() > 0) {
        			for(PlanResult plan:plans) {
        		        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");

        			}
    			} else {
    		        buf.append("見つかりませんでした");
    			}
    		} else if (type.equals("selectRentalResource")) {
        			String category = (String) contexts.get("category");
        			Collection<PlanResult> plans = null;
        			if (null == category || category.isEmpty()) {
            			plans = sqlexecuter.selectRentalResource("aic");
        			} else {
            			plans = sqlexecuter.selectRentalResource(category,"aic");
        			}
        			if (plans.size() > 0) {
            			for(PlanResult plan:plans) {
            		        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");
            			}
        			} else {
        		        buf.append("見つかりませんでした");
        			}
    		} else if (type.equals("selectResource")) {
    			String rental = null;
    			String bihin = null;
    			String sysNumber = null;
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("category")) {
    					rental=entity.getValue();
    				}

    				if (bihin == null && entity.getEntity().equals("bihin")) {
    					bihin=entity.getValue();
    				}

    				if (sysNumber == null && entity.getEntity().equals("sys-number")) {
    					sysNumber=entity.getValue();
    				}
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
    			if (null == time || time.isEmpty()) {
    				DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss");
    				LocalDateTime d = LocalDateTime.now();
    				time = d.format(f);
    			}
    			int plans = sqlexecuter.insertPlanResult(rental, "1", date + " " + time, date + " 9:00:00", dateto + " 17:30:00");
    		}
    	return buf.toString();
    }
}