package jp.co.jrits.yoyakutter_bot.watson;

import java.util.List;
import java.util.Map;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.Entity;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

public class YoyakuConversation {
    private ConversationService service;

	public YoyakuConversation() {
        service = new ConversationService(ConversationService.VERSION_DATE_2016_09_20,"b1ccec5b-5750-4eb2-91b0-9ac7e5375ab9", "JdmGAhIfBnGS");

	}

	public YoyakuConvEntity reply(Map<String,Object>context,String content) {
        // sync
        MessageRequest newMessage;
        if (null == context){
            newMessage = new MessageRequest.Builder().inputText(content).build();
        } else {
            newMessage = new MessageRequest.Builder().inputText(content).context(context).build();
        }
        MessageResponse response = service.message("7ff1fcc6-7317-4493-aa31-83c528ec4b17", newMessage).execute();
        //MessageResponse response = service.message("66485b56-3f43-4c45-ada3-7ac2e99cb8e5", newMessage).execute();


        Map<String,Object> map = response.getOutput();
        System.out.println(map.toString());

		return new YoyakuConvEntity(response.getContext(),map.get("text").toString().replace("[", "").replace("]",""),response.getEntities());

	}

	public YoyakuConvEntity setContext(Map<String,Object>context) {
        // sync
        MessageRequest newMessage = new MessageRequest.Builder().context(context).build();
        MessageResponse response = service.message("7ff1fcc6-7317-4493-aa31-83c528ec4b17", newMessage).execute();
        //MessageResponse response = service.message("66485b56-3f43-4c45-ada3-7ac2e99cb8e5", newMessage).execute();


        Map<String,Object> map = response.getOutput();
        System.out.println(map.toString());

		return new YoyakuConvEntity(response.getContext(),map.get("text").toString().replace("[", "").replace("]",""),response.getEntities());

	}

	public class YoyakuConvEntity {
		private Map<String,Object> context;
		private String message;
		private List<Entity> entities;


		public YoyakuConvEntity(Map<String,Object> context, String message,List<Entity> entities) {
			this.context = context;
			this.message = message;
			this.entities=entities;
		}

		public Map<String, Object> getContext() {
			return context;
		}
		public void setContext(Map<String, Object> context) {
			this.context = context;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}

		public List<Entity> getEntities() {
			return entities;
		}

		public void setEntities(List<Entity> entities) {
			this.entities = entities;
		}

	}
}
