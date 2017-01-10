/* 	Field class that simulates the events of a baseball field for BaseballSim
	by Ian Zapolsky 6/13/12 */

import java.util.Random;
import java.io.*;

public class Field {

	Player[] field, basepaths;	//field�� �������� ��Ÿ��, 0���� ����, �� ���� ��Ȳ�� ����
	int outs;					//�ƿ�ī��Ʈ
	final int WALK = 0;			//������ 0
	final int SINGLE = 1;		//��Ÿ�� 1
	final int DOUBLE = 2;		//2��Ÿ�� 2
	final int TRIPLE = 3;		//3��Ÿ�� 3
	final int HOMERUN = 4;		//Ȩ���� 4
	final int HBP = 5;			//�籸�� 5
	final int STRIKEOUT = 6;	//������ 6
	final int OUT = 7;			//�ƿ��� 7
	PrintWriter w;
	
	public Field(PrintWriter init_w) {	//������, �μ��� PrintWriter�� �޾Ƽ� ������ ������ ������ �Ҵ�
		w = init_w;					//�μ��� �޾ƿ� PrintWriter�� ������ ������ ������ �Ҵ�
		basepaths = new Player[4];	//�� ���� ��Ȳ�� ��Ÿ��
		outs = 0;					//�ƿ�ī��Ʈ 0���� �ʱ�ȭ
	}

	public void resetField(Player[] init_field) {	//�ʵ� �ʱ�ȭ �Լ�, �μ��� ���� �迭�� �޾ƿ�
		field = init_field;		//�μ��� �޾ƿ� ���� �迭�� ������ ������ ������ ����
		clearBasepaths();		//�� �縦 ���� �Լ�
		outs = 0;				//�ƿ�ī��Ʈ 0���� �ʱ�ȭ
	}
		
	private void clearBasepaths() {	//�� �縦 �ʱ�ȭ �ϴ� �Լ�
		for (int i = 0; i < 4; i++) //�縦 ���� for��
			basepaths[i] = null;
	}

	public int updateField(int result, Player player) {	//�ʵ带 ������Ʈ�ϴ� �Լ�, �μ��� Ÿ�� ����� ���������� �޾ƿ�
		if (result == WALK) {						//����� ������ ���
			printLog(player+" draws a walk.\n");	//���� �α� ���
			calcPitchCount();						//�������� ����ϴ� �޼ҵ�
			return updateWalk(player);				//�������� ���� ��������� ��ȯ��
		}
		else if (result == HBP) {
			printLog(player+" is hit by pitch.\n");
			calcPitchCount();
			return updateHBP(player);
		}
		else if (result == STRIKEOUT) {
			printLog(player+" strikes out.\n");
			calcPitchCount();
			return updateStrikeout(player);	
		}
		else if (result == HOMERUN) {
			printLog(player+" hits a home run.\n");
			calcPitchCount();
			return updateHomerun(player);
		}
		else if (result == TRIPLE) {
			printLog(player+" hits a triple.\n");
			calcPitchCount();
			return updateTriple(player);
		}
		else if (result == DOUBLE) {
			printLog(player+" hits a double.\n");
			calcPitchCount();
			return updateDouble(player);
		}
		else if (result == SINGLE) {				//������� ���� �ش� ����� ���� ����������, ��������� ��ȯ��
			printLog(player+" hits a single.\n");
			calcPitchCount();
			return updateSingle(player);
		}
		else {										//�ƿ����� ���� ���� ����� ��ȯ
			printLog(player+" hits a fieldable ball to ");
			calcPitchCount();
			return updateOut(player);
		}
	}

	private int updateOut(Player player) {	//�ƿ����� ���� �ʵ� ��Ȳ�� ����
		int runs = 0;
		field[0].to++;								//������ ���� �ƿ�ī��Ʈ�� ���� ����
		boolean sac = false;						//�±׾� ��������
		Random rand = new Random();					//�����ӽ� ����
		int hitDestination = rand.nextInt(9);		//Ÿ������, Ÿ�� ������ 0~8���� �������� �����µ� �̰͵� ������ ����
		double error = rand.nextDouble();			//����Ȯ��
		printLog((hitDestination+1)+".\n");			//Ÿ������ ���
		
		if (error > field[hitDestination].fldPCT) {	//������ �������� ���� �������� �ش� �߼��� ��ũ���ͺ��� ũ�� ����
			printLog(field[hitDestination]+" commits a fielding error!\n");
			field[hitDestination].e++;				//�ش� �߼� ����+
			if (manOnThird())						//3�翡 ���ڰ� ���� ���
				runs += advanceFrom(3,1);			//3�翡�� 1�� ������ ������ ����
			if (manOnSecond())						//2�翡 ���ڰ� ���� ���
				runs += advanceFrom(2,1);			//2�翡�� 1�� ������ ������ ����
			if (manOnFirst())						//1�翡 ���ڰ� ���� ���
				runs += advanceFrom(1,1);			//1�翡�� 1�� ������ ������ ����
			placeRunner(1,player);					//1�翡 �μ��� ���޹��� ������ ��ġ��Ŵ
		}
		
		else if (outs < 2 && hitDestination > 5 && (manOnThird() || manOnSecond())) {
			//������ �ƴϰ�, �ƿ�ī��Ʈ�� 2�Ʒ��̸� Ÿ�������� �ܾ��̸�, 1�糪 2�翡 ���ڰ� ���� ��� = ����ö��� ��Ȳ
			if (manOnThird()) {							//3�翡 ���ڰ� ���� ���
				printLog("The hit is a sac fly.\n");	
				runs += advanceFrom(3,1);				//3�翡�� 1�� ������ ������ ����
				player.rbi++;							//�μ��� ���� ������ Ÿ�� ����
				sac = true;								//���Ÿ Ȱ��ȭ
			}
			if (manOnSecond() && hitDestination == 8) {	//2�翡 ���ڰ� �ְ�, Ÿ�� ������ �߰߼��� ���
				printLog("The hit is a sac fly.\n");
				runs += advanceFrom(2,1);				//2�翡�� 1�� ������ ������ ����
				sac = true;								//���Ÿ Ȱ��ȭ
			}
			outs++;										//�ƿ�ī��Ʈ ����
			field[0].to++;								//������ �ƿ�ī��Ʈ�� ����
		}
		
		/*	TODO: Implement a better double play model */
		else if (outs < 2 && hitDestination > 1 && hitDestination < 6 && manOnFirst()) {
			//�ƿ�ī��Ʈ�� 2�� ���ϰ�, Ÿ�� ������ 1�̻�, 2,3,4,5 ����, �� �����̸�, 1�翡 ���ڰ� ���� ��� = �����÷��� ��Ȳ
			boolean scored = false;					//
			printLog("The infield turns a double play!\n");
			if (outs + 2 == 3) {					//�ƿ�ī��Ʈ�� 1�� ���
				outs += 2;							//�ƿ�ī��Ʈ 2�� �߰�
				field[0].to += 2;					//������ �ƿ�ī��Ʈ 2�� �߰�
			}
			else {									//�ƿ�ī��Ʈ�� 0�� ���
				if (manOnThird()) {					//3�翡 ���ڰ� ���� ���
					runs += advanceFrom(3,1);		//������ 3�翡�� 1�� ������ ������ ����
					scored = true;					
				}
				if (manOnSecond()) 					//2�翡 ���ڰ� ���� ���
					runs += advanceFrom(2,1);		//������ 2�翡�� 1�� ������ ������ ����
				clearBase(2);						//2�縦 ���
				clearBase(1);						//1�縦 ���
				field[0].to += 2;					//������ �ƿ�ī��Ʈ�� 2�� ����
				outs += 2;							//�ƿ�ī��Ʈ�� 2�� ����
			}
		}
		else					//�̿��� ���
			outs++;				//�ƿ�ī��Ʈ ����
		
		
		
		if (sac == true)		//���Ÿ ��Ȳ�̸�
			/* do nothing */;	//Ÿ���� ������Ű�� ����
		else					//�ƴ� ���
			player.ab++;		//Ÿ���� ������Ŵ
		return runs;			//������ ��ȯ
	}		

	private int updateSingle(Player player) {	//��Ÿ�� ���� ��Ȳ ����
		int runs = 0;								//��ȯ�� ���� ����
		field[0].h++;								//������ �Ǿ�Ÿ ����
		
		if (manOnThird()) {							//3�翡 ���ڰ� ���� ���
			runs += advanceFrom(3,1);				//������ 3�翡�� 1�� ������ ������ ����
			player.rbi++;							//Ÿ�� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnSecond()) {						//2�翡 ���ڰ� �ִ� ���
			if (player.isFast()) {					//Ÿ�ڰ� ���� ���, �ٵ� �̰� 2�� ������ �ӵ��� üũ�ؾ���
				runs += advanceFrom(2,2);			//������ 2�翡�� 2�� ����
				player.rbi++;						//Ÿ���� Ÿ�� ����
				field[0].er++;						//������ ��å�� ����
			}
			else
				runs += advanceFrom(2,1);			//������ 2�翡�� 1�� ����
		}
		if (manOnFirst()) {							//1�翡 ���ڰ� �ִ� ���
			if (!manOnThird() && player.isFast())	//3�翡 ���ڰ� ����, Ÿ�ڰ� ���� ���, �� ��쿡�� ������ �ӵ��� üũ�ؾ���
				runs += advanceFrom(1,2);			//1�翡�� 2�� ����
			else
				runs += advanceFrom(1,1);			//���� ��� 1�翡�� 1�� ����
		}
		placeRunner(1,player);						//Ÿ�ڸ� 1�翡 ��ġ��Ŵ
		player.ab++;								//Ÿ���� Ÿ�� ����
		player.hit++;								//Ÿ���� ��Ÿ ����
		return runs;								//���� ��ȯ
	}

	private int updateDouble(Player player) {		//2��Ÿ �ϰ�� ��Ȳ ����
		int runs = 0;								
		field[0].h++;								//���� �Ǿ�Ÿ ����
		
		if (manOnThird()) {							//3�翡 ���ڰ� ���� ���
			runs += advanceFrom(3,2);				//3�翡�� 2�� ����
			player.rbi++;							//Ÿ�� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnSecond()) {						//2�翡 ���ڰ� ���� ���
			runs += advanceFrom(2,2);				//2�翡�� 2�� ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnFirst()) {							//1�翡 ���ڰ� ���� ���
			if (player.isFast()) {					//Ÿ�ڰ� ���� ���, �̰͵� ������ �ӵ��� üũ�ؾ���
				runs += advanceFrom(1,3);			//1�翡�� 3�� ����
				player.rbi++;						//Ÿ�� Ÿ�� ����
				field[0].er++;						//���� ��å�� ����
			}
			else									//Ÿ�ڰ� ���� ���
				runs += advanceFrom(1,2);			//1�翡�� 2�� ����
		}
		
		placeRunner(2,player);						//Ÿ�ڸ� 2�翡 ��ġ
		player.ab++;								//Ÿ���� Ÿ�� ����
		player.hit++;								//Ÿ���� ��Ÿ ����
		return runs;								//���� ��ȯ
	}
		
	private int updateTriple(Player player) {		//3��Ÿ�� ��� ��Ȳ ����
		int runs = 0;
		field[0].h++;								//������ �Ǿ�Ÿ ����
		
		if (manOnThird()) {							//3�翡 ���ڰ� ���� ���
			runs += advanceFrom(3,3);				//3�翡�� 3ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnSecond()) {						//2�翡 ���ڰ� ���� ���
			runs += advanceFrom(2,3);				//2�翡�� 3ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnFirst()) {							//1�翡 ���ڰ� ���� ���
			runs += advanceFrom(1,3);				//1�翡�� 3ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		placeRunner(3,player);						//Ÿ�ڸ� 3�翡 ��ġ
		player.ab++;								//Ÿ���� Ÿ�� ����
		player.hit++;								//Ÿ���� ��Ÿ ����
		return runs;								//���� ��ȯ
	}


	private int updateHomerun(Player player) {		//Ȩ���� ��� ��Ȳ ����
		int runs = 0;								
		field[0].h++;								//������ �Ǿ�Ÿ ����
		field[0].er++;								//������ ��å�� ����

		if (manOnThird()) {							//3�翡 ���ڰ� ���� ���
			runs += advanceFrom(3,4);				//3�翡�� 4ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnSecond()) {						//2�翡 ���ڰ� ���� ���
			runs += advanceFrom(2,4);				//2�翡�� 4ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		if (manOnFirst()) {							//1�翡 ���ڰ� ���� ���
			runs += advanceFrom(1,4);				//1�翡�� 4ĭ ����
			player.rbi++;							//Ÿ���� Ÿ�� ����
			field[0].er++;							//������ ��å�� ����
		}
		
		player.rbi++;								//Ÿ���� Ÿ�� ����
		player.ab++;								//Ÿ���� Ÿ�� ����
		player.r++;									//Ÿ���� ���� ����
		printLog("\t"+player+" scores.\n");
		player.hit++;								//Ÿ���� ��Ÿ ����
		runs++;										//���� �߰�
		return runs;								//���� ��ȯ
	}

	private int updateStrikeout(Player player) {	//������ ��� ��Ȳ ����
		int runs = 0;								
		field[0].so++;								//������ ���� ����
		field[0].to++;								//������ �ƿ� ����
		Random rand = new Random();					//�����ӽ� ����
		double error = rand.nextDouble();			//����Ȯ�� ���
		
		if (error > field[1].fldPCT) {				//����Ȯ���� ���ͺ��� ũ�� ��������
			// catcher makes an error: passed ball
			field[1].e++;							//������ ���� ����
			printLog(field[1]+" drops the third strike!\n");
			
			//�� �翡 ���ڰ� �־��� ��� �� �羿 ����
			if (manOnThird())						
				runs += advanceFrom(3,1);
			if (manOnSecond())
				runs += advanceFrom(2,1);
			if (manOnFirst())
				runs += advanceFrom(1,1);
			
			placeRunner(1,player);//Ÿ�ڸ� 1�翡 ��ġ	
		}
		else 
			outs++;									//������ �ȳ��� ��� �ƿ�����
		player.ab++;								//Ÿ���� Ÿ�� ����
		return runs;
	}
		
	private int updateWalk(Player player) {			//������ ������ ���� ����
		int runs = 0;								//�� ��Ȳ�� ������ ���� ���� ���
		field[0].bb++;								//������ ����ī��Ʈ�� ����
		if (manOnThird())							//3�翡 ������ ��ġ���� ���
			if (manOnSecond() && manOnFirst()) {	//1, 2�翡�� ������ ���� ���
				runs += advanceFrom(3,1);			//Ÿ�ڰ� 3�翡�� 1�� �������� �� ������ ��Ȳ
				player.rbi++;						//Ÿ���� Ÿ���� ������Ŵ.
				field[0].er++;						//������ ��å���� ������Ŵ
			}
		if (manOnSecond()) {						//2�翡 ������ ��ġ���� ���
			if (manOnFirst()) {						//1�翡 ������ ��ġ���� ���
				runs += advanceFrom(2,1);			//2�翡�� 1�� �������� �� ������ ��Ȳ
			}
		}
		if (manOnFirst()) {							//1�翡 ������ ��ġ���� ���
			runs += advanceFrom(1,1);				//1�翡�� 1�� �������� �� ������ ��Ȳ
		}
		placeRunner(1,player);						//�μ��� ���޹��� ������ 1�翡 ��ġ��Ŵ
		return runs;								//���� ����� ��ȯ
	}

	private int updateHBP(Player player) {			//�籸 ��Ȳ�� ���
		int runs = 0;
		field[0].hbp++;								//������ �籸 ����
		
		if (manOnThird())							//���ڰ� 3�翡 ��ġ���� ���
			if (manOnSecond() && manOnFirst()) {	//���ڰ� 1��, 2�翡�� ���� ���
				runs += advanceFrom(3,1);			//3�翡�� 1ĭ ����
				player.rbi++;						//Ÿ���� Ÿ�� ����
				field[0].er++;//					//������ ��å�� ����
		}
		
		if (manOnSecond())							//���ڰ� 2�翡 ���� ���
			if (manOnFirst()) {						//���ڰ� 1�翡 ���� ���
				runs += advanceFrom(2,1);			//���ڸ� 2�翡�� 1ĭ ����
		}
		
		if (manOnFirst()) {							//���ڰ� 1�翡 ���� ���
			runs += advanceFrom(1,1);				//���ڸ� 1�翡�� 1ĭ ����
		}
		
		placeRunner(1,player);						//Ÿ�ڸ� 1�翡 ��ġ��Ŵ
		return runs;
	}

	private int advanceFrom(int base, int bases) {	//�������� �����ų �� ���� �޼ҵ�
		int runs = 0;
		if ((base+bases) > 3) {				//���� ���̽��� �����ϴ� ���� ���� 3�� ������ ������, ���� ��Ȳ�� ���
			basepaths[0] = basepaths[base];	//basepaths[0]�� Ÿ����, Ÿ���� �޾ƿ� ���̽� �ѹ��� ������ ��ü
			basepaths[base] = null;	//�޾ƿ� ���̽� �ѹ��� �����ڸ��� ���
			runs++;	//1�� ����
			basepaths[0].r++;//�޾ƿ� ���̽� ������ ������ ����
			printLog("\t"+basepaths[0]+" scores.\n");//���� �α� ���
		}
		else {	//���� ��Ȳ�� �ƴ� ���
			basepaths[base+bases] = basepaths[base];	//���� ��ġ�� ������ �����Ų ��ġ�� �ٲ�
			basepaths[base] = null;						//���� ��ġ�� ���
			printLog("\t"+basepaths[base+bases]+" advances to "+(base+bases)+"B.\n");	//�α� ���
		}
		return runs;//���縦 ����ϰ�, ������ ���� ��� ����
	}

	private void calcPitchCount() {	//������ ����ϴ� �Լ�
		field[0].pitchCount = ((4.81*field[0].so) + (5.14*field[0].bb) + (3.27*(field[0].h+field[0].hbp)) + (3.16*(field[0].to - field[0].so)));
	}	


	private void printLog(String log) {
		//System.out.print(log);
		w.print(log);
	}

	private String stateOfBasepaths() {
		String state = "";
		if (!manOnFirst() && !manOnSecond() && !manOnThird())
			state = "Nobody on base.\n";
		else if (manOnFirst() && manOnSecond() && manOnThird())
			state = "Man on 1B, 2B, and 3B\n";
		else if (manOnFirst() && !manOnSecond() && manOnThird())
			state = "Man on 1B and 3B.\n";
		else if (manOnFirst() && manOnSecond() && !manOnThird())
			state = "Man on 1B and 2B.\n";	
		else if (manOnFirst() && !manOnSecond() && !manOnThird())
			state = "Man on 1B.\n";
		else if (!manOnFirst() && manOnSecond() && !manOnThird())
			state = "Man on 2B.\n";
		else if (!manOnFirst() && !manOnSecond() && manOnThird())
			state = "Man on 3B.\n";
		else if (!manOnFirst() && manOnSecond() && manOnThird())
			state = "Man on 2B and 3B.\n";
		return state;
	}
	
	private void clearBase(int base) {	//�μ��� �޾ƿ� ������ �ش��ϴ� ���� ���ڸ� ���
		basepaths[base] = null;
	}

	private void placeRunner(int base, Player player) {	//�μ��� ���޹��� ������ �μ��� ���޹��� ������ ���̽��� ��ġ��Ŵ
		basepaths[base] = player;
		printLog("\t"+player+" reaches "+base+"B.\n");
	}

	private boolean manOnFirst() {	//1�翡 ���ڰ� �ִ��� Ȯ��
		if (basepaths[1] != null)
			return true;
		else
			return false;
	}
	private boolean manOnSecond() {	//2�翡 ���ڰ� �ִ��� Ȯ��
		if (basepaths[2] != null)
			return true;
		else
			return false;
	}
	private boolean manOnThird() {	//3�翡 ���ڰ� �ִ��� Ȯ��
		if (basepaths[3] != null)
			return true;
		else
			return false;
	}
	
	public int getOuts() { 			//�ƿ� ī��Ʈ�� ����
		return outs; 
	}
		 		
}		

	

