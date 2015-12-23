import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class DataProce {
	public static void main(String[] arg){
		try{
			String dataInPath=arg[0];
			String dataOutPath=arg[1];
			File dataIn=new File(dataInPath);
			File dataOut=new File(dataOutPath);
			String lineTxt = null;
			String[] split;
			String[] tags={"weekend","weekday","day","night","free","low","medium","high","Manhattan","NJ","Brooklyn","other","Music" 
					,"Business & Professional","Other","Performing & Visual Arts","Health & Wellness","Food & Drink","Sports & Fitness"
					,"Family & Education","Community & Culture","Religion & Spirituality","Charity & Causes","Fashion & Beauty"
					,"Hobbies & Special Interest","Seasonal & Holiday","Travel & Outdoor","Science & Technology","Home & Lifestyle"
					,"Auto, Boat & Air","Government & Politics","Film, Media & Entertainment"};
			int [] feature={2,2,3,3,4,4,4,4,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6};

			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(dataIn)));
			BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataOut)));
			while((lineTxt = reader.readLine()) != null){
                  split=lineTxt.split(";;"); 
                  writer.write(split[0]+";;"+split[1]);
                  for(int i=0;i<tags.length;i++){
                	  if(tags[i].equals(split[feature[i]])){
                		  writer.write(";;1");
                	  }
                	  else{
                		  writer.write(";;0");
                	  }
                  }
                  writer.newLine();
			}	
			reader.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		String s="1	23";
		System.out.println(s.substring(2));
	}

}
