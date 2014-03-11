/*
	GO BACK N ARQ---------SENDER

	ACK's are not affected by noise
*/


import java.io.*;
import java.net.*;
import java.util.*;	//for Random function

class DataOfSender
{
	static InputStreamReader r;
	static BufferedReader br;
	static BufferedWriter writer;
	static DataInputStream input;
 	static DataOutputStream output;

	public static ServerSocket s;
	Socket s1=new Socket("localhost",1023);

	static boolean TimeOut;
	static int slideWindow[];
	static int sizeOfWindow;
	static int temp;
	static int generator;	//to generate the sequence nos
	static int not_Acknowledged;
	static int canBeSent;



	DataOfSender()throws IOException,InterruptedException
	{
	r=new InputStreamReader(System.in);
	br=new BufferedReader(r);
	writer = new BufferedWriter(new OutputStreamWriter(s1.getOutputStream()));
	input= new DataInputStream(s1.getInputStream());
	output= new DataOutputStream(s1.getOutputStream());

	TimeOut=false;
	not_Acknowledged=0;	//initially both point to the
	canBeSent=0;		//beginning of the frame
	
		System.out.println("Enter the range of sequence nos in terms of powers of 2");
		temp=Integer.parseInt(br.readLine());

	generator=(int)Math.pow(2,temp);	//no typecasting results in loss of precision error at compile time	
	sizeOfWindow=(int)generator-1;		//since size of window is 2^m-1
	slideWindow=new int[1000];		//sizeofwindow only sets the window size..actual window can be infinite

		initializeWindow();

		start(); 
	}

/*----------------------------------initializeWindow---------------------------------------------------------*/

	void initializeWindow()
	{

	/* This method fills the window with the reqd sequence nos in order */

		for(int i=0;i<1000;i++)
		slideWindow[i]=i%generator;	//if here it was sizeofwindow then sequence nos would be 1 less

	return;			

	}

/*----------------------------------start()---------------------------------------------------------*/

	void start()throws IOException,InterruptedException
	{
	int sentFrame;
	boolean sizeLimit=false;


		

		while(true)
		{
			if(canBeSent-not_Acknowledged>=sizeOfWindow)	//window is full
			{
			sizeLimit=true;
			System.out.println("Window full...Initializing waitForACK");
			waitForACK();
			}			
			else
			{
			
				System.out.println("Sending frame with sequence no "+slideWindow[canBeSent]);

				sentFrame=makeFrame(slideWindow[canBeSent]);

				System.out.println("Sent frame after noise effect "+sentFrame);		//Debugging
				System.out.println("Press 1 to send frame...0 to wait for ACK");	//Debugging
				int choice=Integer.parseInt(br.readLine());				//Debugging
		
				if(choice==1)
				{
				sendFrame(sentFrame);
				canBeSent++;	
				}
				else
				waitForACK();
			}
			
		}
	}


/*----------------------------------improvedWaitTime---------------------------------------------------------*/

	synchronized void improvedWaitTime()throws IOException,InterruptedException
	{
		for(int i=0;i<100;i++)
			if(input.available()!=0)	//to check if anything is present on buffer
			return;				//if true stop the timer and check if it is a valid
			else				//ACK else wait
			wait(100);			//available() checks no of bytes waiting in buffer
							//thus if receiver doesnt send ACK for loop will execute
							//and time out will be triggered
	}

/*----------------------------------makeFrame----------------------------------------------------------------*/

	int makeFrame(int canBeSent)
	{
	int sentFrame;
	Random noise=new Random();	/*
					We could have used Random noise=new Random(generator)
					This produces only +ve sequence nos and therefore
					the noise will only be additive..

					In order to give it a more realistic effect of both	
					additive and diminitive we use no parameters so that 
					-ve nos too may be generated.

					We take care of the range of sequence nos using '%'
					*/ 

	sentFrame=canBeSent;

	sentFrame=(noise.nextInt()%2)+canBeSent;  	  //this produces noise and generates only in between the
							  //range of sequence nos...
							  //factor is 2 since now noise can either affect or not affect

 							  /*
 							  use
							  noise.nextInt()%generator+canBeSent to get invalid nos too
							  */	

	return sentFrame;
		
	}

/*-------------------------------------sendFrame---------------------------------------------------------------*/

	void sendFrame(int sentFrame)throws IOException
	{
	String toSend;
		
			toSend=Integer.toString(sentFrame);

			writer.write(toSend);
			writer.newLine();
			writer.flush();

	}

/*----------------------------------waitForACK-----------------------------------------------------------*/

	void waitForACK()throws IOException,InterruptedException
	{
	/* variable can be sent is just for the gobackN method without it being passed here is is 
	   not possible to execute gobackN since it needs both pointers in the window
	*/

	int receivedACK;
	String toReceive=new String();

		while(true)
		{
			improvedWaitTime();

			if(input.available()==0)	//again check buffer...obviously empty
			{
			TimeOut=true;			//Time out has occured..execute goBackN
			System.out.println("----------TIME OUT-----------");
			goBackN();
			return;
			}

		toReceive=input.readLine();
		receivedACK=Integer.parseInt(toReceive);
			

			while(receivedACK>slideWindow[not_Acknowledged])
			not_Acknowledged++;

		System.out.println("ACK RECEIVED");
			
		}

	}
/*-------------------------------------------------------------------------------------------------------------*/

	void goBackN()throws IOException	//here no noise!!
	{
	int temp=not_Acknowledged;	//since we dont want to change not_Acknowledged unless it is really
					//Acknowledged

		while(temp<canBeSent)
		{
			System.out.println("Resending frame " +slideWindow[temp]);
	
			sendFrame(slideWindow[temp]);

			temp++;
		}
	}

}

class Sender
{
	public static void main(String args[])throws IOException,InterruptedException
	{
	DataOfSender d=new DataOfSender();
	}
}