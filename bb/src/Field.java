/* 	Field class that simulates the events of a baseball field for BaseballSim
	by Ian Zapolsky 6/13/12 */

import java.util.Random;
import java.io.*;

public class Field {

	Player[] field, basepaths;	//field는 선수들을 나타냄, 0부터 투수, 각 루의 상황을 저장
	int outs;					//아웃카운트
	final int WALK = 0;			//볼넷은 0
	final int SINGLE = 1;		//단타는 1
	final int DOUBLE = 2;		//2루타는 2
	final int TRIPLE = 3;		//3루타는 3
	final int HOMERUN = 4;		//홈런은 4
	final int HBP = 5;			//사구는 5
	final int STRIKEOUT = 6;	//삼진은 6
	final int OUT = 7;			//아웃은 7
	PrintWriter w;
	
	public Field(PrintWriter init_w) {	//생성자, 인수로 PrintWriter를 받아서 위에서 선언한 변수에 할당
		w = init_w;					//인수로 받아온 PrintWriter를 위에서 선언한 변수에 할당
		basepaths = new Player[4];	//각 루의 상황을 나타냄
		outs = 0;					//아웃카운트 0으로 초기화
	}

	public void resetField(Player[] init_field) {	//필드 초기화 함수, 인수로 선수 배열을 받아옴
		field = init_field;		//인수로 받아온 선수 배열을 위에서 선언한 변수에 저장
		clearBasepaths();		//각 루를 비우는 함수
		outs = 0;				//아웃카운트 0으로 초기화
	}
		
	private void clearBasepaths() {	//각 루를 초기화 하는 함수
		for (int i = 0; i < 4; i++) //루를 비우는 for문
			basepaths[i] = null;
	}

	public int updateField(int result, Player player) {	//필드를 업데이트하는 함수, 인수로 타격 결과와 선수정보를 받아옴
		if (result == WALK) {						//결과가 볼넷일 경우
			printLog(player+" draws a walk.\n");	//볼넷 로그 출력
			calcPitchCount();						//투구수를 계산하는 메소드
			return updateWalk(player);				//볼넷으로 인한 득점결과를 반환함
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
		else if (result == SINGLE) {				//여기까지 전부 해당 결과로 인한 투구수정리, 득점결과를 반환함
			printLog(player+" hits a single.\n");
			calcPitchCount();
			return updateSingle(player);
		}
		else {										//아웃으로 인한 득점 결과를 반환
			printLog(player+" hits a fieldable ball to ");
			calcPitchCount();
			return updateOut(player);
		}
	}

	private int updateOut(Player player) {	//아웃으로 인한 필드 상황을 정리
		int runs = 0;
		field[0].to++;								//투수가 잡은 아웃카운트의 총합 증가
		boolean sac = false;						//태그업 가능한지
		Random rand = new Random();					//랜덤머신 생성
		int hitDestination = rand.nextInt(9);		//타구방향, 타구 방향이 0~8까지 랜덤으로 나오는데 이것도 비율이 있음
		double error = rand.nextDouble();			//에러확률
		printLog((hitDestination+1)+".\n");			//타구방향 출력
		
		if (error > field[hitDestination].fldPCT) {	//위에서 랜덤으로 나온 에러값이 해당 야수의 파크팩터보다 크면 에러
			printLog(field[hitDestination]+" commits a fielding error!\n");
			field[hitDestination].e++;				//해당 야수 에러+
			if (manOnThird())						//3루에 주자가 있을 경우
				runs += advanceFrom(3,1);			//3루에서 1루 진루한 점수를 더함
			if (manOnSecond())						//2루에 주자가 있을 경우
				runs += advanceFrom(2,1);			//2루에서 1루 진루한 점수를 더함
			if (manOnFirst())						//1루에 주자가 있을 경우
				runs += advanceFrom(1,1);			//1루에서 1루 진루한 점수를 더함
			placeRunner(1,player);					//1루에 인수로 전달받은 선수를 위치시킴
		}
		
		else if (outs < 2 && hitDestination > 5 && (manOnThird() || manOnSecond())) {
			//에러가 아니고, 아웃카운트가 2아래이며 타구방향이 외야이며, 1루나 2루에 주자가 있을 경우 = 희생플라이 상황
			if (manOnThird()) {							//3루에 주자가 있을 경우
				printLog("The hit is a sac fly.\n");	
				runs += advanceFrom(3,1);				//3루에서 1루 진루한 점수를 더함
				player.rbi++;							//인수로 받은 선수의 타점 증가
				sac = true;								//희생타 활성화
			}
			if (manOnSecond() && hitDestination == 8) {	//2루에 주자가 있고, 타구 방향이 중견수일 경우
				printLog("The hit is a sac fly.\n");
				runs += advanceFrom(2,1);				//2루에서 1루 진루한 점수를 더함
				sac = true;								//희생타 활성화
			}
			outs++;										//아웃카운트 증가
			field[0].to++;								//투수의 아웃카운트수 증가
		}
		
		/*	TODO: Implement a better double play model */
		else if (outs < 2 && hitDestination > 1 && hitDestination < 6 && manOnFirst()) {
			//아웃카운트가 2개 이하고, 타구 방향이 1이상, 2,3,4,5 방향, 즉 내야이며, 1루에 주자가 있을 경우 = 더블플레이 상황
			boolean scored = false;					//
			printLog("The infield turns a double play!\n");
			if (outs + 2 == 3) {					//아웃카운트가 1일 경우
				outs += 2;							//아웃카운트 2개 추가
				field[0].to += 2;					//투수의 아웃카운트 2개 추가
			}
			else {									//아웃카운트가 0인 경우
				if (manOnThird()) {					//3루에 주자가 있을 경우
					runs += advanceFrom(3,1);		//득점에 3루에서 1루 진루한 점수를 더함
					scored = true;					
				}
				if (manOnSecond()) 					//2루에 주자가 있을 경우
					runs += advanceFrom(2,1);		//득점에 2루에서 1루 진루한 점수를 더함
				clearBase(2);						//2루를 비움
				clearBase(1);						//1루를 비움
				field[0].to += 2;					//투수의 아웃카운트에 2를 더함
				outs += 2;							//아웃카운트에 2를 더함
			}
		}
		else					//이외의 경우
			outs++;				//아웃카운트 증가
		
		
		
		if (sac == true)		//희생타 상황이면
			/* do nothing */;	//타수를 증가시키지 않음
		else					//아닐 경우
			player.ab++;		//타수를 증가시킴
		return runs;			//득점을 반환
	}		

	private int updateSingle(Player player) {	//단타로 인한 상황 정리
		int runs = 0;								//반환할 점수 선언
		field[0].h++;								//투수의 피안타 증가
		
		if (manOnThird()) {							//3루에 주자가 있을 경우
			runs += advanceFrom(3,1);				//득점에 3루에서 1루 진루한 점수를 더함
			player.rbi++;							//타자 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnSecond()) {						//2루에 주자가 있는 경우
			if (player.isFast()) {					//타자가 빠른 경우, 근데 이건 2루 주자의 속도를 체크해야함
				runs += advanceFrom(2,2);			//득점에 2루에서 2루 진루
				player.rbi++;						//타자의 타점 증가
				field[0].er++;						//투수의 자책점 증가
			}
			else
				runs += advanceFrom(2,1);			//득점에 2루에서 1루 진루
		}
		if (manOnFirst()) {							//1루에 주자가 있는 경우
			if (!manOnThird() && player.isFast())	//3루에 주자가 없고, 타자가 빠른 경우, 이 경우에도 주자의 속도를 체크해야함
				runs += advanceFrom(1,2);			//1루에서 2루 진루
			else
				runs += advanceFrom(1,1);			//느린 경우 1루에서 1루 진루
		}
		placeRunner(1,player);						//타자를 1루에 위치시킴
		player.ab++;								//타자의 타수 증가
		player.hit++;								//타자의 안타 증가
		return runs;								//득점 반환
	}

	private int updateDouble(Player player) {		//2루타 일경우 상황 정리
		int runs = 0;								
		field[0].h++;								//투수 피안타 증가
		
		if (manOnThird()) {							//3루에 주자가 있을 경우
			runs += advanceFrom(3,2);				//3루에서 2루 진루
			player.rbi++;							//타자 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnSecond()) {						//2루에 주자가 있을 경우
			runs += advanceFrom(2,2);				//2루에서 2루 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnFirst()) {							//1루에 주자가 있을 경우
			if (player.isFast()) {					//타자가 빠를 경우, 이것도 주자의 속도를 체크해야함
				runs += advanceFrom(1,3);			//1루에서 3루 진루
				player.rbi++;						//타자 타점 증가
				field[0].er++;						//투수 자책점 증가
			}
			else									//타자가 느릴 경우
				runs += advanceFrom(1,2);			//1루에서 2루 진루
		}
		
		placeRunner(2,player);						//타자를 2루에 위치
		player.ab++;								//타자의 타수 증가
		player.hit++;								//타자의 안타 증가
		return runs;								//득점 반환
	}
		
	private int updateTriple(Player player) {		//3루타일 경우 상황 정리
		int runs = 0;
		field[0].h++;								//투수의 피안타 증가
		
		if (manOnThird()) {							//3루에 주자가 있을 경우
			runs += advanceFrom(3,3);				//3루에서 3칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnSecond()) {						//2루에 주자가 있을 경우
			runs += advanceFrom(2,3);				//2루에서 3칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnFirst()) {							//1루에 주자가 있을 경우
			runs += advanceFrom(1,3);				//1루에서 3칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		placeRunner(3,player);						//타자를 3루에 위치
		player.ab++;								//타자의 타수 증가
		player.hit++;								//타자의 안타 증가
		return runs;								//득점 반환
	}


	private int updateHomerun(Player player) {		//홈런일 경우 상황 정리
		int runs = 0;								
		field[0].h++;								//투수의 피안타 증가
		field[0].er++;								//투수의 자책점 증가

		if (manOnThird()) {							//3루에 주자가 있을 경우
			runs += advanceFrom(3,4);				//3루에서 4칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnSecond()) {						//2루에 주자가 있을 경우
			runs += advanceFrom(2,4);				//2루에서 4칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		if (manOnFirst()) {							//1루에 주자가 있을 경우
			runs += advanceFrom(1,4);				//1루에서 4칸 진루
			player.rbi++;							//타자의 타점 증가
			field[0].er++;							//투수의 자책점 증가
		}
		
		player.rbi++;								//타자의 타점 증가
		player.ab++;								//타자의 타수 증가
		player.r++;									//타자의 득점 증가
		printLog("\t"+player+" scores.\n");
		player.hit++;								//타자의 안타 증가
		runs++;										//득점 추가
		return runs;								//득점 반환
	}

	private int updateStrikeout(Player player) {	//삼진일 경우 상황 정리
		int runs = 0;								
		field[0].so++;								//투수의 삼진 증가
		field[0].to++;								//투수의 아웃 증가
		Random rand = new Random();					//랜덤머신 선언
		double error = rand.nextDouble();			//에러확률 계산
		
		if (error > field[1].fldPCT) {				//에러확률이 팩터보다 크게 나왔으면
			// catcher makes an error: passed ball
			field[1].e++;							//포수의 에러 증가
			printLog(field[1]+" drops the third strike!\n");
			
			//각 루에 주자가 있었을 경우 한 루씩 진루
			if (manOnThird())						
				runs += advanceFrom(3,1);
			if (manOnSecond())
				runs += advanceFrom(2,1);
			if (manOnFirst())
				runs += advanceFrom(1,1);
			
			placeRunner(1,player);//타자를 1루에 위치	
		}
		else 
			outs++;									//에러가 안났을 경우 아웃증가
		player.ab++;								//타자의 타수 증가
		return runs;
	}
		
	private int updateWalk(Player player) {			//볼넷이 나왔을 때의 정리
		int runs = 0;								//이 상황이 끝났을 때의 득점 결과
		field[0].bb++;								//투수의 볼넷카운트를 증가
		if (manOnThird())							//3루에 선수가 위치했을 경우
			if (manOnSecond() && manOnFirst()) {	//1, 2루에도 선수가 있을 경우
				runs += advanceFrom(3,1);			//타자가 3루에서 1루 진루했을 때 점수의 상황
				player.rbi++;						//타자의 타점을 증가시킴.
				field[0].er++;						//투수의 자책점을 증가시킴
			}
		if (manOnSecond()) {						//2루에 선수가 위치했을 경우
			if (manOnFirst()) {						//1루에 선수가 위치했을 경우
				runs += advanceFrom(2,1);			//2루에서 1루 진루했을 때 점수의 상황
			}
		}
		if (manOnFirst()) {							//1루에 선수가 위치했을 경우
			runs += advanceFrom(1,1);				//1루에서 1루 진루했을 때 점수의 상황
		}
		placeRunner(1,player);						//인수로 전달받은 선수를 1루에 위치시킴
		return runs;								//득점 결과를 반환
	}

	private int updateHBP(Player player) {			//사구 상황일 경우
		int runs = 0;
		field[0].hbp++;								//투수의 사구 증가
		
		if (manOnThird())							//주자가 3루에 위치했을 경우
			if (manOnSecond() && manOnFirst()) {	//주자가 1루, 2루에도 있을 경우
				runs += advanceFrom(3,1);			//3루에서 1칸 진루
				player.rbi++;						//타자의 타점 증가
				field[0].er++;//					//투수의 자책점 증가
		}
		
		if (manOnSecond())							//주자가 2루에 있을 경우
			if (manOnFirst()) {						//주자가 1루에 있을 경우
				runs += advanceFrom(2,1);			//주자를 2루에서 1칸 진루
		}
		
		if (manOnFirst()) {							//주자가 1루에 있을 경우
			runs += advanceFrom(1,1);				//주자를 1루에서 1칸 진루
		}
		
		placeRunner(1,player);						//타자를 1루에 위치시킴
		return runs;
	}

	private int advanceFrom(int base, int bases) {	//선수들을 진루시킬 때 쓰는 메소드
		int runs = 0;
		if ((base+bases) > 3) {				//기준 베이스와 진루하는 루의 합이 3을 넘으면 득점임, 득점 상황일 경우
			basepaths[0] = basepaths[base];	//basepaths[0]은 타석임, 타석을 받아온 베이스 넘버의 선수로 교체
			basepaths[base] = null;	//받아온 베이스 넘버의 선수자리를 비움
			runs++;	//1점 득점
			basepaths[0].r++;//받아온 베이스 선수의 득점을 증가
			printLog("\t"+basepaths[0]+" scores.\n");//증가 로그 출력
		}
		else {	//득점 상황이 아닐 경우
			basepaths[base+bases] = basepaths[base];	//기준 위치의 선수를 진루시킨 위치로 바꿈
			basepaths[base] = null;						//기준 위치를 비움
			printLog("\t"+basepaths[base+bases]+" advances to "+(base+bases)+"B.\n");	//로그 출력
		}
		return runs;//진루를 계산하고, 득점이 있을 경우 리턴
	}

	private void calcPitchCount() {	//투구수 계산하는 함수
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
	
	private void clearBase(int base) {	//인수로 받아온 정수에 해당하는 루의 주자를 비움
		basepaths[base] = null;
	}

	private void placeRunner(int base, Player player) {	//인수로 전달받은 선수를 인수로 전달받은 정수의 베이스에 위치시킴
		basepaths[base] = player;
		printLog("\t"+player+" reaches "+base+"B.\n");
	}

	private boolean manOnFirst() {	//1루에 주자가 있는지 확인
		if (basepaths[1] != null)
			return true;
		else
			return false;
	}
	private boolean manOnSecond() {	//2루에 주자가 있는지 확인
		if (basepaths[2] != null)
			return true;
		else
			return false;
	}
	private boolean manOnThird() {	//3루에 주자가 있는지 확인
		if (basepaths[3] != null)
			return true;
		else
			return false;
	}
	
	public int getOuts() { 			//아웃 카운트를 리턴
		return outs; 
	}
		 		
}		

	

