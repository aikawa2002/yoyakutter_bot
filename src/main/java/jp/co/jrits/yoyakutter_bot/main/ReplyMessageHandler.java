package jp.co.jrits.yoyakutter_bot.main;

import java.io.IOException;
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

/*    	if (exception != null) {
            resp.reply(exception.getMessage());

    	}
*/
        Map<String,Object> context = (Map<String,Object>) session.getAttribute("context", null);

        YoyakuConvEntity nextConv = yoyakuConv.reply(context, content);

		// メッセージを送信したユーザーのメンションを取得する
		String mention = req.getUserDisp();

		List<Entity> entities=nextConv.getEntities();
		String type = (String) nextConv.getContext().get("type");

		for (Entity entity:entities) {
			System.out.println(entity.getEntity() + entity.getValue());
		}

		resp.reply(nextConv.getMessage());
		if (type != null) {
            try {
            	resp.reply(execute(type,mention,entities));
    		} catch (Exception e) {
    			// TODO 自動生成された catch ブロック
    			e.printStackTrace();
    	        resp.reply(e.getMessage());
    		}
        }
        session.setAttribute("context", nextConv.getContext());
    }

    private String execute(String type,String mention,List<Entity> entities) throws Exception {
    		StringBuffer buf = new StringBuffer();
            System.out.println(type);
    		if (type.equals("searchPlanTable")) {
    			String category = null;
    			List<String> sysDate=new ArrayList<>();
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("rental")) {
    					category=entity.getValue();
    				}
    				if (entity.getEntity().equals("sys-date")) {
    					sysDate.add(entity.getValue());
    				}
    				System.out.println(entity.getEntity() + entity.getValue());
    			}
    			Collection<PlanResult> plans = sqlexecuter.selectPlanResult(category,"aic",sysDate);
    			if (plans.size() > 0) {
        			for(PlanResult plan:plans) {
        		        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");

        			}
    			} else {
    		        buf.append("見つかりませんでした");
    			}
    		} else if (type.equals("selectResource")) {
    			String rental = null;
    			List<String> sysDate=new ArrayList<>();
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("category")) {
    					rental=entity.getValue();
    				}
    				System.out.println(entity.getEntity() + entity.getValue());
    			}
    			Collection<Resource> resources = sqlexecuter.selectResource(rental);
    			if (resources.size() > 0) {
        			for(Resource resource:resources) {
        		        buf.append(resource.getResourceName() +"\n");
        			}
    			} else {
    		        buf.append("見つかりませんでした");
    			}
    		} else if (type.equals("insertPlanTable")) {
    			String rental = null;
    			List<String> sysDate=new ArrayList<>();
    			for (Entity entity:entities) {
    				if (entity.getEntity().equals("rental")) {
    					rental=entity.getValue();
    				}
    				if (entity.getEntity().equals("sys-date")) {
    					sysDate.add(entity.getValue());
    				}
    				System.out.println(entity.getEntity() + entity.getValue());
    			}
    			Collection<PlanResult> plans = sqlexecuter.selectPlanResult(rental,"aic",sysDate);
    			if (plans.size() > 0) {
        			for(PlanResult plan:plans) {
        		        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");

        			}
    			} else {
    		        buf.append("見つかりませんでした");
    			}
            } else if (type.equals("selectRentalResource")) {
                List<String> sysDate=new ArrayList<>();
                for (Entity entity:entities) {
                    if (entity.getEntity().equals("sys-date")) {
                        sysDate.add(entity.getValue());
                    }
                    System.out.println(entity.getEntity() + entity.getValue());
                }
                Collection<PlanResult> plans = sqlexecuter.selectRentalResource("aic");
                if (plans.size() > 0) {
                    for(PlanResult plan:plans) {
                        buf.append(plan.getResourceName() + ":" + plan.getUserName()+":" + plan.getStartTime()+":" + plan.getFinishTime()+"\n");

                    }
                } else {
                    buf.append("見つかりませんでした");
                }
    		}
    	return buf.toString();
    }
}