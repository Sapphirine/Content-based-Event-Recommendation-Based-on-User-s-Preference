import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Recomendation {
	private static final int featureNumber=32;
	private static double[] posterior;
	private String eventDataPath;
	private String[] recommendedEvents;
	private String userDataPath;
	private double[] userdata=new double[featureNumber];
	private int numReco;
	private String userID;
	
	public Recomendation(String eventDataPath,String trainingOutPath){
		this.eventDataPath=eventDataPath;
		this.userDataPath=trainingOutPath;
	}
	
	private void getUserData(){
		try{
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(userDataPath)));
			String trainingResult;
			while ((trainingResult=reader.readLine())!=null && !trainingResult.substring(0, userID.length()).equals(userID)){}
			
			if(trainingResult==null){
				return;
			}
			String[] split=trainingResult.substring(userID.length()+1).split(";;");
			for(int i=0;i<featureNumber;i++){
				userdata[i]=Double.parseDouble(split[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void recommend(String userID,int numReco){
		try {
			this.numReco=numReco;
			this.userID=userID;
			getUserData();
			double switchtemp;
			String temps;
			String event;
			String[] split;
			recommendedEvents=new String[numReco+1];
			posterior=new double[numReco+1];
			BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(eventDataPath)));
			while((event=reader.readLine())!=null){
				split=event.split(";;");
				posterior[numReco]=1;
				for(int i=0;i<featureNumber;i++){
					if(split[i+2].equals("1")){
						posterior[numReco]*=userdata[i];
					}
				}
				recommendedEvents[numReco]=split[0]+";;"+split[1];
				int i=numReco;
				while(i>0 && posterior[i]>posterior[i-1]){
					switchtemp=posterior[i-1];
					temps=recommendedEvents[i-1];
					posterior[i-1]=posterior[i];
					recommendedEvents[i-1]=recommendedEvents[i];
					posterior[i]=switchtemp;
					recommendedEvents[i]=temps;
					i--;
					//System.out.println(i);
				}
			}		
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showRecommendedEvents(){
		System.out.println("Recommended events for user:"+userID);
		for(int i=0;i<numReco;i++){
			System.out.println(recommendedEvents[i]);
		}
	}
	
	
	
	public static void main(String[] args){
		try {
			String trainingOutPath=args[0];
			String eventDataPath=args[1];
			String userID=args[2];
			int numReco=Integer.parseInt(args[3]);
			
			Recomendation recommender=new Recomendation(eventDataPath, trainingOutPath);
			recommender.recommend(userID, numReco);
			recommender.showRecommendedEvents();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
