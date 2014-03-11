/*
	GO BACK N ARQ----RECEIVER

	ACK's are not affected by noise

	since ACK not initialized
	the garbage value of ACK will decide which frame to accept first which may cause a problem
	if it is not zero
*/

import java.io.*;
import java.net.*;

class DataOfReceiver
{
	static DataInputStream input;
 	static DataOutputStream output;
	static InputStreamReader r;
	static BufferedReader br;
	static BufferedWriter writer;

	static int receiveWindow[];
        static int generator;
	static int ACK;
	static int temp;


		ServerSocket s;
		Socket s1;


	DataOfReceiver()throws IOException,InterruptedException
	{
	r=new InputStreamReader(System.in);
	br=new BufferedReader(r);

		System.out.println("Enter the range of sequence nos in terms of powers of 2");
		temp=Integer.parseInt(br.readLine());

		generator=(int)Math.pow(2,temp);			
		receiveWindow=new int[1000];

			initializeWindow();		
	
	s=new ServerSocket(1023);
	s1=s.accept();



	writer = new BufferedWriter(new OutputStreamWriter(s1.getOutputStream()));

	input= new DataInputStream(s1.getInputStream());
	output= new DataOutputStream(s1.getOutputStream());

	

		start();
	}

/*----------------------------------initializeWindow---------------------------------------------------------*/

	void initializeWindow()
	{

	/* This method fills the window with the reqd sequence nos in order */

		for(int i=0;i<1000;i++)
		receiveWindow[i]=i%generator;	//if here it was sizeofwindow then sequence nos would be 1 less

	return;			

	}

	void start()throws IOException,InterruptedException
	{
	boolean validFrame;
	boolean timerMatures=false;

		while(true)
		{

			validFrame=checkFrame();			


				if(validFrame)
				{
				ACK++;
				}
			
			timerMatures=improvedWaitTime();

		if(timerMatures)	//no frames received in time specified...send ACK to prevent retransmission
		{
		timerMatures=false;	//reset the timer

		System.out.println("--------TIMER MATURED---------");
		System.out.println("Sending ACK with ACK no "+receiveWindow[ACK]);

		sendACK();		//send ACK
		}
			
	
		}
	}

	boolean checkFrame()throws IOException
	{
	String toReceive;
	int received_no;

		
		toReceive=input.readLine();
		received_no=Integer.parseInt(toReceive);

			if(received_no==receiveWindow[ACK])
			return true;

	return false;
	}

/*----------------------------------improvedWaitTime---------------------------------------------------------*/

	synchronized boolean improvedWaitTime()throws IOException,InterruptedException
	{
		for(int i=0;i<50;i++)
			if(input.available()!=0)	
			return false;				
			else				
			wait(100);			
							
							

	return true;
	}

/*-------------------------------------sendFrame---------------------------------------------------------------*/

	void sendACK()throws IOException
	{
	String toSend;
		
			toSend=Integer.toString(receiveWindow[ACK]);

		//System.out.println("Press 1 to send ACK");	Debugging
		//int choice=Integer.parseInt(br.readLine());	Debugging

			writer.write(toSend);
			writer.newLine();
			writer.flush();

	}


}

class Receiver
{
	public static void main(String args[])throws IOException,InterruptedException
	{
	DataOfReceiver d=new DataOfReceiver();
	}
}