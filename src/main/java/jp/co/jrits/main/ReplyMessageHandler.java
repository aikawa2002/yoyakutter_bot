package jp.co.jrits.main;

import java.io.IOException;
import java.util.Map;

import org.riversun.slacklet.SlackletRequest;
import org.riversun.slacklet.SlackletResponse;
import org.riversun.slacklet.SlackletSession;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;

/**
 *
 * @author
 *
 */
public class ReplyMessageHandler {

    private ConversationService service;
    private Map<String,Object>context = null;

    public ReplyMessageHandler() {
        service = new ConversationService(ConversationService.VERSION_DATE_2016_09_20,"b1ccec5b-5750-4eb2-91b0-9ac7e5375ab9", "JdmGAhIfBnGS");
        VisualRecognition visualService = new VisualRecognition("","");

    }

    public void reply(SlackletRequest req ,SlackletResponse resp) throws IOException {

        String content = req.getContent();

        // セッションを取得する（セッションはユーザー毎に固有）
        SlackletSession session = req.getSession();
        System.out.println("-------");
        System.out.println(session);
        System.out.println("-------");

        // 発言回数カウント用のintegerをセッションから取得する。未だ何も入れていないときは、デフォルト値１とする
        Integer num = (Integer) session.getAttribute("num", 1);

        resp.reply(req.getUserDisp() + "さんは" + num + "回目に「" + content + "」って言いました。");

        // 回更をインクリメントして、セッションを更新する
        num++;
        session.setAttribute("num", num);


        // sync
        MessageRequest newMessage;
        if (null == context){
            newMessage = new MessageRequest.Builder().inputText(content).build();
        } else {
            newMessage = new MessageRequest.Builder().inputText(content).context(context).build();
        }
        MessageResponse response = service.message("6130d07b-e1ce-48b4-b645-514a6b419ff2", newMessage).execute();

        System.out.println(response);

/*        String replyToken = event.getReplyToken();
        List<Message> messages = new ArrayList<>();
        Map<String,Object> map = response.getOutput();
        context = response.getContext();
        String rep = map.get("text").toString();
        messages.add(new TextMessage(rep.replace("[", "").replace("]", "")));

        return lineMessagingService
                .replyMessage(new ReplyMessage(replyToken, messages))
                .execute()
                .body();
*/    }

}