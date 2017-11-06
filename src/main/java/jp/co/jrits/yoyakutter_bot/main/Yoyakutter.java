package jp.co.jrits.yoyakutter_bot.main;

import java.io.IOException;
import java.util.ResourceBundle;

import org.riversun.slacklet.Slacklet;
import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletService;

public class Yoyakutter {

    public static void main(String[] args) throws IOException {

    	ReplyMessageHandler handler = new ReplyMessageHandler();

        String botToken = ResourceBundle.getBundle("credentials").getString("slack.bot_api_token");

        SlackletService slackService = new SlackletService(botToken);

        slackService.addSlacklet(new Slacklet() {


            @Override
            public void onDirectMessagePosted(SlackletRequest req, SlackletResponse resp) {
                boolean mensionFlg = false;

                try {
                    handler.reply(req, resp,mensionFlg);
                } catch (IOException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }


            }

            @Override
            public void onMentionedMessagePosted(SlackletRequest req, SlackletResponse resp) {
                // あるチャンネルでこのBOTへのメンション付きメッセージがポストされた(例　「@smilebot おはよう」）
                boolean mensionFlg = true;

                try {
                    handler.reply(req, resp,mensionFlg);
                } catch (IOException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }

            }

        });

        slackService.start();

    }

}
