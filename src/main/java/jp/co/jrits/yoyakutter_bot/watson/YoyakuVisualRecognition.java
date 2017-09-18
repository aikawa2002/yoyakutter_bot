package jp.co.jrits.yoyakutter_bot.watson;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

public class YoyakuVisualRecognition {
    private VisualRecognition service;

    public YoyakuVisualRecognition() {
	    VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
	    service.setApiKey("be8d30b3076015d2d4bd753d372fd5fa6b56483c");
    }

    public String classify(Path path) {
	    ClassifyImagesOptions options =
		          new ClassifyImagesOptions.Builder().images(new File(path.toAbsolutePath().toString())).build();

		    VisualClassification result = service.classify(options).execute();
		    List<ImageClassification> list = result.getImages();


		    return list.get(0).getClassifiers().get(0).getClasses().get(0).getName();
    }

}
