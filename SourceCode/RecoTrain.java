import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class RecoTrain {
	private static final int featureNumber=32;
	private static final int featureIndex=4;
	
	public static class LongArrayWritable extends ArrayWritable{
		public LongArrayWritable() {
			 super(LongWritable.class);
		}
		
		public LongArrayWritable(long[] longs) {
			 super(LongWritable.class);
			 LongWritable[] writableLongs = new LongWritable[longs.length];
			 for (int i = 0; i < longs.length; i++) {
				 writableLongs[i] = new LongWritable(longs[i]);
			 }
			 set(writableLongs);
		}
		
		public String toString(){
			LongWritable[] longs=(LongWritable[]) this.get();
			String string=Long.toString(longs[0].get());
			
			for(int i=1;i<longs.length;i++){
				string=string+";;"+Long.toString(longs[i].get());
			}
			return string;
		}
	}
	
	public static class DoubleArrayWritable extends ArrayWritable{
		public DoubleArrayWritable() {
			 super(DoubleWritable.class);
		}
		
		public DoubleArrayWritable(double[] doubles) {
			 super(DoubleWritable.class);
			 DoubleWritable[] writableDoubles = new DoubleWritable[doubles.length];
			 for (int i = 0; i < doubles.length; i++) {
				 writableDoubles[i] = new DoubleWritable(doubles[i]);
			 }
			 set(writableDoubles);
		}
		
		public String toString(){
			DoubleWritable[] doubles=(DoubleWritable[]) this.get();
			String string=Double.toString(doubles[0].get());
			
			for(int i=1;i<doubles.length;i++){
				string=string+";;"+Double.toString(doubles[i].get());
			}
			return string;
		}
	}
	
	
	public static class MyMap extends Mapper<Object, Text, LongWritable, LongArrayWritable>{	
		private LongWritable userID=new LongWritable();
		private long[] features=new long[featureNumber+1];//array of the event attributes and user preference
		
		public void map(Object key, Text eventFeatures, Context context) throws IOException, InterruptedException {
			String[] split = eventFeatures.toString().split(";;");
			userID.set(Long.parseLong(split[0]));
			features[0]=Long.parseLong(split[1]);
			for(int i=0;i<featureNumber;i++){
				features[i+1]=Long.parseLong(split[featureIndex+i]);
			}
			context.write(userID, new LongArrayWritable(features));
		}
	}
	
	public static class MyReduce extends Reducer<LongWritable, LongArrayWritable, LongWritable, DoubleArrayWritable> {
		private long[][] featureCount=new long[2][featureNumber];
		private double[] likelihood=new double[featureNumber];
		
		
		public void reduce(LongWritable key, Iterable<LongArrayWritable> features, Context context)
				throws IOException, InterruptedException {
			
			for(int i=0;i<featureNumber;i++){
				for(int j=0;j<2;j++){
					featureCount[j][i]=0;
				}
			}
			for (LongArrayWritable f : features) {
				for(int i=0;i<featureNumber;i++){
					featureCount[(int) ((LongWritable)(f.get()[0])).get()][i]=
							featureCount[(int) ((LongWritable)(f.get()[0])).get()][i]+((LongWritable)(f.get()[i+1])).get();
				}
			}
			for(int i=0;i<featureNumber;i++){
				likelihood[i]=featureCount[1][i]/(featureCount[1][i]+featureCount[0][i]+0.0001);
			}
			context.write(key, new DoubleArrayWritable(likelihood));
		}
	}
	
	public static void main(String[] args){
		
		try{
			
			File file=new File(args[1]);
			if(file.exists()){
				if (file.isDirectory()) {  
			        File[] ff = file.listFiles();  
			        for (int i = 0; i < ff.length; i++) {  
			            ff[i].delete();  
			        }  
			    }  
			    file.delete();  	
			}
			
			Configuration conf = new Configuration();

			conf.set("mapred.job.tracker", "192.168.243.255:9001");
			if (args.length != 2) {
				System.err.println("Usage: BayesianClassifier <in> <out>");
				System.exit(2);
			}
			System.out.println(args[0]);
			System.out.println(args[1]);
		
			Job job = Job.getInstance(conf, "Bayesian Classifier Training");
			job.setJarByClass(RecoTrain.class);
		
			job.setMapperClass(MyMap.class);
			job.setReducerClass(MyReduce.class);
		

			job.setOutputKeyClass(LongWritable.class);
			job.setOutputValueClass(LongArrayWritable.class);
		
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			System.exit(job.waitForCompletion(true) ? 0 : 1);
		}catch (Exception e) {
				e.printStackTrace();
		}
	}
}

