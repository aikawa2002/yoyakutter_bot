package jp.co.jrits.yoyakutter_bot.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.riversun.slacklet.Slacklet;
import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletService;
import org.riversun.slacklet.SlackletSession;

public class Yoyakutter {
	private static HashMap<SlackletSession, String> sessionMap;

    public static void main(String[] args) throws IOException {

        String botToken = ResourceBundle.getBundle("credentials").getString("slack.bot_api_token");
        sessionMap = new HashMap<>();

        SlackletService slackService = new SlackletService(botToken);

        slackService.addSlacklet(new Slacklet() {

            @Override
            public void onDirectMessagePosted(SlackletRequest req, SlackletResponse resp) {
            	String oContent = sessionMap.get(req.getSession());
            	if (oContent == null || !oContent.equals(req.getContent())) {
                    System.out.println("###" + req.getSender().getRealName() + "#########################");
                    sessionMap.put(req.getSession(), req.getContent());

	                boolean mensionFlg = false;

	                try {
	                    ReplyMessageHandler handler = new ReplyMessageHandler();
	                    handler.reply(req, resp,mensionFlg);
	                } catch (IOException e) {
	                    // TODO 自動生成された catch ブロック
	                    e.printStackTrace();
	                }
            	}
            }

            @Override
            public void onMentionedMessagePosted(SlackletRequest req, SlackletResponse resp) {
                // あるチャンネルでこのBOTへのメンション付きメッセージがポストされた(例　「@smilebot おはよう」）
            	String oContent = sessionMap.get(req.getSession());
            	if (oContent == null || !oContent.equals(req.getContent())) {
                    System.out.println("$$$" + req.getSender().getRealName() + "$$$$$$$$$$$$$$$$$$$$$$$$$");
                    sessionMap.put(req.getSession(), req.getContent());
	                boolean mensionFlg = true;

	                try {
	                    ReplyMessageHandler handler = new ReplyMessageHandler();
	                    handler.reply(req, resp,mensionFlg);
	                } catch (IOException e) {
	                    // TODO 自動生成された catch ブロック
	                    e.printStackTrace();
	                }
            	}
            }

        });

        slackService.start();

    }

}
