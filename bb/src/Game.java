/*	Game class for BaseballSim
	by Ian Zapolsky 6/6/13 */

/* 	TODO: Standardize the usage of player vs batter variable names.
	TODO: Improve pitcher subbing logic
	TODO: Capture pitcher stats (W,L,SAV) */


import java.util.Random;
import java.io.*;

public class Game {
	
	final int WALK = 0;			//볼넷은 0으로 정의
    final int SINGLE = 1;		//단타는 1
    final int DOUBLE = 2;		//2루타는 2
    final int TRIPLE = 3;		//3루타는 3
    final int HOMERUN = 4;		//홈런은 4
    final int HBP = 5;			//사구는 5
    final int STRIKEOUT = 6;	//삼진은 6
    final int OUT = 7;			//아웃은 7
	League league;				//리그 클래스 변수선언
	Team home, away;			//홈팀, 어웨이팀 저장
	Player[] homeOrder, awayOrder;	//홈팀타순, 어웨이타순
	Player[] homeField, awayField;	//홈팀수비, 어웨이수비
	Player homeP, awayP;			//홈팀투수, 어웨이투수
	int inning;					//현재 이닝
	int outsBefore;				//상황 진행전 아웃카운트
	int hRuns;					//홈팀 점수
	int aRuns;					//어웨이팀 점수
	int hSpot;					//홈팀 타순
	int aSpot;					//어웨이팀 타순
	
	Random rand;				//랜덤머신 선언
	File f;						
	PrintWriter w;				
	
	public Game() { }	

	public void playGame(Team init_home, Team init_away, League init_league, int[] homeInts, int[] awayInts) throws IOException {
		//인수로 홈팀, 어웨이팀, 리그, int 배열은 의미 불명
		w = new PrintWriter(new File("gamelog.txt"));//파일쓰기
		league = init_league;						//인수로 받아온 리그로 리그 할당
		home = init_home;							//홈팀에 인수로 받은 홈팀 할당
		homeField = new Player[9];					//홈팀수비에 새로운 선수배열 할당
		homeOrder = new Player[9];					//홈팀타순에 새로운 선수배열 할당

		away = init_away;							//어웨이팀에 인수로 받은 어웨이팀 할당
		awayField = new Player[9];					//어웨이수비에 새로운 선수배열 할당
		awayOrder = new Player[9];					//어웨이타순에 새로운 선수배열 할당

		for (int i = 0; i < 9; i++) {
			homeOrder[i] = home.roster.get(homeInts[i]);	//홈팀 타순 할당
			awayOrder[i] = away.roster.get(awayInts[i]);	//어웨이팀 타순 할당
		}
		homeP = home.roster.get(homeInts[9]);				//홈팀투수에 투수 할당
		awayP = away.roster.get(awayInts[9]);				//어웨이투수에 투수 할당
		buildFields();										

		hRuns = aRuns = hSpot = aSpot = 0;
		inning = 1;
		
		printLog("-------------------------------------------------------------------------------------------\n");
		printLog("Welcome to SimBaseball. Today's game is between the "+home+" and the "+away+"\n");
		printLog("Starting pitcher for the "+home+" is "+homeP+". Starting pitcher for the "+away+" is "+awayP+"\n");
		printLog("-------------------------------------------------------------------------------------------\n\n");
		
		// outline of game structure
		
		Field diamond = new Field(w);

		while (aRuns == hRuns || inning < 10) {
			printLog("Top of "+inning+". "+home+": "+hRuns+" "+away+": "+aRuns+"\n\n");
			checkPitcherForSub(homeP);
			diamond.resetField(homeField);
			while ((outsBefore = diamond.getOuts()) < 3) {
				printLog(awayOrder[aSpot]+" is up to bat.\n");
				aRuns += diamond.updateField(matchup(homeP, awayOrder[aSpot], calcBattingAdjustment(homeP, awayOrder[aSpot])), awayOrder[aSpot]);
				increment_aSpot();
				if (outsBefore < diamond.getOuts())
					printLog(""+diamond.getOuts()+" outs!\n");
			}
			printLog("Bottom of "+inning+". "+home+": "+hRuns+" "+away+": "+aRuns+"\n\n");
			checkPitcherForSub(awayP);
			diamond.resetField(awayField);
			while ((outsBefore = diamond.getOuts()) < 3) {
				if (hRuns > aRuns && inning > 8)
					break;
				printLog(homeOrder[hSpot]+" is up to bat.\n");
				hRuns += diamond.updateField(matchup(homeP, homeOrder[hSpot], calcBattingAdjustment(awayP, homeOrder[hSpot])), homeOrder[hSpot]);
				increment_hSpot();
				if (outsBefore < diamond.getOuts())
					printLog(""+diamond.getOuts()+" outs!\n");
			}
			inning++;
		}

		// print winner, create line score

		if (aRuns > hRuns) {
			printLog(""+away+" win, "+aRuns+" to "+hRuns+"\n\n");
		}
		else {
			printLog(""+home+" win, "+hRuns+" to "+aRuns+"\n\n");
		}
		w.close();
		
	}

	private double calcBattingAdjustment(Player pitcher, Player batter) {
		if (pitcher.LR.equals("R")) {
			if (batter.LR.equals("R"))
				return -.015;
			else
				return .030;
		}
		else {
			if (batter.LR.equals("L"))	
				return -.090;
			else
				return .045;
		}
	}

	private double calcPitchingAdjustment(Player pitcher) {
		return -.05+(pitcher.pitchCount*.001);
	}

	private int matchup(Player pitcher, Player player, double adjustment) {

		// 	Batter Stats  
		double SingleAVG = player.singleAVG;
		double DoubleAVG = player.doubleAVG;
		double TripleAVG = player.tripleAVG;
		double HRAVG = player.HRAVG;
		double BBAVG = player.BBAVG;
		double SOAVG = player.SOAVG;
		double HBPAVG = player.HBPAVG;

		// 	Adjust batting stats with normalized adjustment
		SingleAVG += (adjustment*(player.singles/player.Hit));
		DoubleAVG += (adjustment*(player.doubles/player.Hit));
		TripleAVG += (adjustment*(player.triples/player.Hit));
		HRAVG += (adjustment*(player.HR/player.Hit));
	
		// 	Pitcher Stats 
		double SingleOVA = pitcher.pSingleAVG;
		double DoubleOVA = pitcher.pDoubleAVG;
		double TripleOVA = pitcher.pTripleAVG;
		double HROVA = pitcher.pHRAVG;
		double BBOVA = pitcher.pBBAVG;
		double SOOVA = pitcher.pSOAVG;
		double HBPOVA = pitcher.pHBPAVG;
				
		// 	Adjust pitching stats according to linear equation based on simulated pitch count
		double adj = calcPitchingAdjustment(pitcher);
		SingleOVA += (adj*SingleOVA);
		DoubleOVA += (adj*DoubleOVA);
		TripleOVA += (adj*TripleOVA);
		HROVA += (adj*HROVA);
		BBOVA += (adj*BBOVA);
		HBPOVA += (adj*BBOVA);

		// 	League Stats 
		double lSingleOVA = league.leagueOVASingle;
		double lDoubleOVA = league.leagueOVADouble;
		double lTripleOVA = league.leagueOVATriple;
		double lHROVA = league.leagueOVAHR;
		double lBBOVA = league.leagueOVABB;
		double lSOOVA = league.leagueOVASO;
		double lHBPOVA = league.leagueOVAHBP;

		// 	Bill James log5 adjusted batter stats
		double aSingleleAVG = adjustStat(SingleAVG, SingleOVA, lSingleOVA);
		double aDoubleAVG = adjustStat(DoubleAVG, DoubleOVA, lDoubleOVA);
		double aTripleAVG = adjustStat(TripleAVG, TripleOVA, lTripleOVA);
		double aHRAVG = adjustStat(HRAVG, HROVA, lHROVA);
		double aBBAVG = adjustStat(BBAVG, BBOVA, lBBOVA);
		double aSOAVG = adjustStat(SOAVG, SOOVA, lSOOVA);
		double aHBPAVG = adjustStat(HBPAVG, HBPOVA, lHBPOVA);
		
		/*	
		System.out.println("\nadjusted statistics\n");
		System.out.println(aSingleleAVG);
		System.out.println(aDoubleAVG);
		System.out.println(aTripleAVG);
		System.out.println(aHRAVG);
		System.out.println(aBBAVG);
		System.out.println(aSOAVG);
		System.out.println(aHBPAVG);
		*/

		// 	Now sim outcome 
		//  rand = new Random(System.currentTimeMillis());
		rand = new Random();
		double gen = rand.nextDouble();
		int result;
		/*	Code system for result so we can avoid the passing of bulky strings.
			Each number corresponds to the last variable in the row of its if statement. */
		if (gen <= aBBAVG)
			result = WALK;
		else if (gen <= aBBAVG+aSingleleAVG)
			result = SINGLE;
		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG)
			result = DOUBLE;
		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG)
			result = TRIPLE;
		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG)
			result = HOMERUN;
		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG+aHBPAVG)
			result = HBP;
		else if (gen <= aBBAVG+aSingleleAVG+aDoubleAVG+aTripleAVG+aHRAVG+aHBPAVG+aSOAVG)
			result = STRIKEOUT;
		else
			result = OUT;
		return result;
	}

	private void checkPitcherForSub(Player pitcher) {
		if (pitcher.pitchCount > 100 && inning < 8) {
			if (pitcher == homeP) {
				printLog("Pitching Substitution: "+home.bullpen.get(6)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
				homeP = home.bullpen.get(6);
				homeOrder[homeOrder.length-1] = homeP;
				addHomeField(homeP);
			}
			else {
				printLog("Pitching Substitution: "+away.bullpen.get(6)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
				awayP = away.bullpen.get(6);
				awayOrder[awayOrder.length-1] =  awayP;
				addHomeField(homeP);
			}
		}
		else if (pitcher == homeP) {
			if (hRuns > aRuns && inning > 7 && pitcher.pitchCount > 50) {
				printLog("Pitching Substitution: Closer "+getCloser(home)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
				homeP = getCloser(home);
				homeOrder[homeOrder.length-1] = homeP;
				addHomeField(homeP);
			}
		}
		else {
			if (aRuns > hRuns && inning > 7) {
				printLog("Pitching Substitution: Closer "+getCloser(away)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
				awayP = getCloser(home);
				awayOrder[awayOrder.length-1] =  awayP;
				addAwayField(awayP);
			}	
		}
	}

	private Player getCloser(Team team) {
		double max = 0;
		Player closer = team.bullpen.get(0);
		for (Player p : team.bullpen) {	//팀의 불펜 배열을 돌려봐서 세이브 가장 많은 사람을 클로저로 올림
			if (p.SAV > max) {
				if ((team == home && p != homeP) || (team == away && p != awayP)) {
					max = p.SAV;
					closer = p;
				}
			}
		}
		return closer;		
	}

		
	private void buildFields() {		//수비진 설정
		for (int i = 0; i < 9; i++) {	//홈팀, 어웨이팀 타순의 선수들 추가하는 for문
			addHomeField(homeOrder[i]);
			addAwayField(awayOrder[i]);
		}
		addHomeField(homeP);			//마지막에 각 팀 투수 추가
		addAwayField(awayP);
	}

	private void addAwayField(Player p) {	//인수로 받은 선수를 어웨이 수비에 추가
		if (p.position.equals("P"))			//투수일 경우
			awayField[0] = p;				//0번에 선수 할당
		else if (p.position.equals("C")) {	//포수일 경우
			if (awayField[1] != null)		//포수자리가 비어있지 않으면
				insertToEmptySlot(awayField, awayField[1], 1, 8);
											//포수 자리의 선수를 야수 자리에 넣음
			awayField[1] = p;				//포수 자리에 선수 넣음
		}
		//이하 각 포지션도 동일 알고리즘
		else if (p.position.equals("1B")) {	
			if (awayField[2] != null)
				insertToEmptySlot(awayField, awayField[2], 1, 8);
			awayField[2] = p;
		}
		else if (p.position.equals("2B")) {
			if (awayField[3] != null)
				insertToEmptySlot(awayField, awayField[3], 1, 8);
			awayField[3] = p;
		}
		else if (p.position.equals("3B")) {
			if (awayField[4] != null)
				insertToEmptySlot(awayField, awayField[4], 1, 8);
			awayField[4] = p;
		}
		else if (p.position.equals("SS")) {
			if (awayField[5] != null)
				insertToEmptySlot(awayField, awayField[5], 1, 8);
			awayField[5] = p;
		}
		else if (p.position.equals("LF")) {
			if (awayField[6] != null)
				insertToEmptySlot(awayField, awayField[6], 1, 8);
			awayField[6] = p;
		}
		else if (p.position.equals("CF")) {
			if (awayField[7] != null)
				insertToEmptySlot(awayField, awayField[7], 1, 8);
			awayField[7] = p;
		}
		else if (p.position.equals("RF")) {
			if (awayField[8] != null)
				insertToEmptySlot(awayField, awayField[8], 1, 8);
			awayField[8] = p;
		}
		else if (p.position.equals("IF"))			//내야수일 경우
			insertToEmptySlot(awayField, p, 2, 5);	//내야수 자리 빈 곳 아무데나 넣음
		else if (p.position.equals("MI"))			//1루 제외한 내야수
			insertToEmptySlot(awayField, p, 3, 5);	//1루 자리 제외한 빈 곳 아무데나 넣음
		else if (p.position.equals("UT"))			//유틸리티, 투수 제외한 전 포지션 가능
			insertToEmptySlot(awayField, p, 1, 8);
		else if (p.position.equals("OF"))			//외야수일 경우
			insertToEmptySlot(awayField, p, 6, 8);
	}

	private void insertToEmptySlot(Player[] field, Player p, int startIndex, int endIndex) {
		//인수로 수비 선수배열, 선수, 시작인덱스, 끝인덱스 받아옴
		for (int i = startIndex; i <= endIndex; i++) {	//시작인덱스부터 끝인덱스까지 for문 돌림
			if (field[i] == null) {						//빈 칸이 있으면
				field[i] = p;							//거기에 선수 추가
				break;
			}
		}
	}

	private void addHomeField(Player p) {	//인수로 받은 선수를 홈 수비에 추가
		if (p.position.equals("P"))			//위의 어웨이팀과 동일 알고리즘
			homeField[0] = p;
		else if (p.position.equals("C")) {
			if (homeField[1] != null)
				insertToEmptySlot(homeField, homeField[1], 1, 8);
			homeField[1] = p;
		}
		else if (p.position.equals("1B")) {
			if (homeField[2] != null)
				insertToEmptySlot(homeField, homeField[2], 1, 8);
			homeField[2] = p;
		}
		else if (p.position.equals("2B")) {
			if (homeField[3] != null)
				insertToEmptySlot(homeField, homeField[3], 1, 8);
			homeField[3] = p;
		}
		else if (p.position.equals("3B")) {
			if (homeField[4] != null)
				insertToEmptySlot(homeField, homeField[4], 1, 8);
			homeField[4] = p;
		}
		else if (p.position.equals("SS")) {
			if (homeField[5] != null)
				insertToEmptySlot(homeField, homeField[5], 1, 8);
			homeField[5] = p;
		}
		else if (p.position.equals("LF")) {
			if (homeField[6] != null)
				insertToEmptySlot(homeField, homeField[6], 1, 8);
			homeField[6] = p;
		}
		else if (p.position.equals("CF")) {
			if (awayField[7] != null)
				insertToEmptySlot(homeField, homeField[7], 1, 8);
			homeField[7] = p;
		}
		else if (p.position.equals("RF")) {
			if (homeField[8] != null)
				insertToEmptySlot(homeField, homeField[8], 1, 8);
			homeField[8] = p;
		}
		else if (p.position.equals("IF"))
			insertToEmptySlot(homeField, p, 2, 5);
		else if (p.position.equals("MI"))
			insertToEmptySlot(homeField, p, 3, 5);
		else if (p.position.equals("UT"))
			insertToEmptySlot(homeField, p, 1, 8);
		else if (p.position.equals("OF"))
			insertToEmptySlot(homeField, p, 6, 8);
	}
	
	private void printField(Player[] field) {
		for (Player p : field)//자바 for문은 변수:배열인데 foreach문과 동일
			System.out.println(p);
	}

	private void printLog(String log) {
		//System.out.print(log);
		w.print(log);
	}

	private void increment_aSpot() {//홈팀 타순 증가
		if (aSpot == 8)
			aSpot = 0;
		else
			aSpot++;
	}
	private void increment_hSpot() {//어웨이팀 타순 증가
		if (hSpot == 8)
			hSpot = 0;
		else
			hSpot++;
	}

	private boolean isOn(int result) {	//사용안함
		if (result < 6)
			return true;
		else
			return false;
	}

	private double adjustStat(double AVG, double OVA, double LOVA) {	//스탯보정
		return ((AVG*OVA)/LOVA)/((((AVG*OVA)/LOVA)+((1-AVG)*(1-OVA)/(1-LOVA))));
	}

}


