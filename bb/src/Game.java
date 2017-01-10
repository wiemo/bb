/*	Game class for BaseballSim
	by Ian Zapolsky 6/6/13 */

/* 	TODO: Standardize the usage of player vs batter variable names.
	TODO: Improve pitcher subbing logic
	TODO: Capture pitcher stats (W,L,SAV) */


import java.util.Random;
import java.io.*;

public class Game {
	
	final int WALK = 0;			//������ 0���� ����
    final int SINGLE = 1;		//��Ÿ�� 1
    final int DOUBLE = 2;		//2��Ÿ�� 2
    final int TRIPLE = 3;		//3��Ÿ�� 3
    final int HOMERUN = 4;		//Ȩ���� 4
    final int HBP = 5;			//�籸�� 5
    final int STRIKEOUT = 6;	//������ 6
    final int OUT = 7;			//�ƿ��� 7
	League league;				//���� Ŭ���� ��������
	Team home, away;			//Ȩ��, ������� ����
	Player[] homeOrder, awayOrder;	//Ȩ��Ÿ��, �����Ÿ��
	Player[] homeField, awayField;	//Ȩ������, ����̼���
	Player homeP, awayP;			//Ȩ������, ���������
	int inning;					//���� �̴�
	int outsBefore;				//��Ȳ ������ �ƿ�ī��Ʈ
	int hRuns;					//Ȩ�� ����
	int aRuns;					//������� ����
	int hSpot;					//Ȩ�� Ÿ��
	int aSpot;					//������� Ÿ��
	
	Random rand;				//�����ӽ� ����
	File f;						
	PrintWriter w;				
	
	public Game() { }	

	public void playGame(Team init_home, Team init_away, League init_league, int[] homeInts, int[] awayInts) throws IOException {
		//�μ��� Ȩ��, �������, ����, int �迭�� �ǹ� �Ҹ�
		w = new PrintWriter(new File("gamelog.txt"));//���Ͼ���
		league = init_league;						//�μ��� �޾ƿ� ���׷� ���� �Ҵ�
		home = init_home;							//Ȩ���� �μ��� ���� Ȩ�� �Ҵ�
		homeField = new Player[9];					//Ȩ������ ���ο� �����迭 �Ҵ�
		homeOrder = new Player[9];					//Ȩ��Ÿ���� ���ο� �����迭 �Ҵ�

		away = init_away;							//��������� �μ��� ���� ������� �Ҵ�
		awayField = new Player[9];					//����̼��� ���ο� �����迭 �Ҵ�
		awayOrder = new Player[9];					//�����Ÿ���� ���ο� �����迭 �Ҵ�

		for (int i = 0; i < 9; i++) {
			homeOrder[i] = home.roster.get(homeInts[i]);	//Ȩ�� Ÿ�� �Ҵ�
			awayOrder[i] = away.roster.get(awayInts[i]);	//������� Ÿ�� �Ҵ�
		}
		homeP = home.roster.get(homeInts[9]);				//Ȩ�������� ���� �Ҵ�
		awayP = away.roster.get(awayInts[9]);				//����������� ���� �Ҵ�
		buildFields();										

		hRuns = aRuns = hSpot = aSpot = 0;					//���� ����, Ÿ�� 0���� �ʱ�ȭ
		inning = 1;											//1�̴׺��� ����
		
		printLog("-------------------------------------------------------------------------------------------\n");
		printLog("Welcome to SimBaseball. Today's game is between the "+home+" and the "+away+"\n");
		printLog("Starting pitcher for the "+home+" is "+homeP+". Starting pitcher for the "+away+" is "+awayP+"\n");
		printLog("-------------------------------------------------------------------------------------------\n\n");
		
		// outline of game structure
		
		Field diamond = new Field(w);						//�ʵ� Ŭ���� ��������

		while (aRuns == hRuns || inning < 10) {					//������ ������ ���ų� Ȥ��(or) 9�̴� ���϶�� ��� �ݺ�
			printLog("Top of "+inning+". "+home+": "+hRuns+" "+away+": "+aRuns+"\n\n");	//�̴��� �� �� ���� ���
			checkPitcherForSub(homeP);							//������ü üũ
			diamond.resetField(homeField);						//�ʵ� Ŭ�����ϰ� Ȩ�� �ʵ� ����
			while ((outsBefore = diamond.getOuts()) < 3) {		//�ƿ� ī��Ʈ�� 2������ ���
				printLog(awayOrder[aSpot]+" is up to bat.\n");	//�α� ���
				aRuns += diamond.updateField(matchup(homeP, awayOrder[aSpot], calcBattingAdjustment(homeP, awayOrder[aSpot])), awayOrder[aSpot]);
				//����� �� ������ 
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

	private double calcPitchingAdjustment(Player pitcher) {		//ü�¿� ���� ������ ���Ȱ��Ҹ� ���
		return -.05+(pitcher.pitchCount*.001);
	}

	private int matchup(Player pitcher, Player player, double adjustment) {
		//�μ��� ����, Ÿ��, �׸��� Ÿ���� Ÿ��Ȯ���� ����. �� �Լ��� ������ Ÿ���� ����� �ùķ��̼��ϰ� ������� ������
		// 	Batter Stats, Ÿ�� Ȯ�� ����
		double SingleAVG = player.singleAVG;					//��Ÿ���� Ÿ�� ��Ÿ�� ����, �Ʒ��� �� ����
		double DoubleAVG = player.doubleAVG;					//2��Ÿ��
		double TripleAVG = player.tripleAVG;					//3��Ÿ��
		double HRAVG = player.HRAVG;							//Ȩ����
		double BBAVG = player.BBAVG;							//������
		double SOAVG = player.SOAVG;							//������
		double HBPAVG = player.HBPAVG;							//�籸��

		// 	Adjust batting stats with normalized adjustment
		SingleAVG += (adjustment*(player.singles/player.Hit));	//��Ÿ�� ����, �Ʒ��� �� ����
		DoubleAVG += (adjustment*(player.doubles/player.Hit));	//2��Ÿ��
		TripleAVG += (adjustment*(player.triples/player.Hit));	//3��Ÿ��
		HRAVG += (adjustment*(player.HR/player.Hit));			//Ȩ����
	
		// 	Pitcher Stats, ���� Ȯ�� ����
		double SingleOVA = pitcher.pSingleAVG;					//��Ÿ���� ���� ��Ÿ�� ����, ���� ����
		double DoubleOVA = pitcher.pDoubleAVG;					
		double TripleOVA = pitcher.pTripleAVG;
		double HROVA = pitcher.pHRAVG;
		double BBOVA = pitcher.pBBAVG;
		double SOOVA = pitcher.pSOAVG;
		double HBPOVA = pitcher.pHBPAVG;
				
		// 	Adjust pitching stats according to linear equation based on simulated pitch count
		double adj = calcPitchingAdjustment(pitcher);	//�������� ���� �ɷ�ġ ����
		SingleOVA += (adj*SingleOVA);					//�� ������ġ�� ����� ����, ���� ����
		DoubleOVA += (adj*DoubleOVA);
		TripleOVA += (adj*TripleOVA);
		HROVA += (adj*HROVA);
		BBOVA += (adj*BBOVA);
		HBPOVA += (adj*BBOVA);

		// 	League Stats,	���� ���� ������
		double lSingleOVA = league.leagueOVASingle;				//���� ��Ÿ�� ������. ���� ����
		double lDoubleOVA = league.leagueOVADouble;
		double lTripleOVA = league.leagueOVATriple;
		double lHROVA = league.leagueOVAHR;
		double lBBOVA = league.leagueOVABB;
		double lSOOVA = league.leagueOVASO;
		double lHBPOVA = league.leagueOVAHBP;

		// 	Bill James log5 adjusted batter stats
		//	http://birdsnest.tistory.com/347
		//	log5 ���� ����
		//	���⼭ ���� ������ ����ϱ����� ��������, ���׽����� ����.
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
		
		rand = new Random();								//�����ӽ� ����
		double gen = rand.nextDouble();						//�����ӽ� ���� ���� ������ ����
		int result;											//������ �����
		/*	Code system for result so we can avoid the passing of bulky strings.
			Each number corresponds to the last variable in the row of its if statement. */
		if (gen <= aBBAVG)
			result = WALK;		//���� ��ġ�� ���ݼ�ġ ���ϸ� ����
		else if (gen <= aBBAVG+aSingleleAVG)
			result = SINGLE;	//���� ��ġ�� ���ݼ�ġ�̻�, ��Ÿ��ġ ���ϸ� ��Ÿ. ���� ���� �˰���
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
		else					//��� ��ġ �� �̻��̸� �ƿ�
			result = OUT;
		
		return result;			//����� ����
	}

	private void checkPitcherForSub(Player pitcher) {	//�μ��� ������ �Է¹���
		if (pitcher.pitchCount > 100 && inning < 8) {	//�Է¹��� ������ �������� 100�� �Ѱ�, �̴��� 7ȸ ���϶��
			if (pitcher == homeP) {						//�μ��ι��� ������ Ȩ�� �������
				printLog("Pitching Substitution: "+home.bullpen.get(6)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
														//�������� : ��ü ���� for ���� ���� leaves the game with ������ pitches ���
				homeP = home.bullpen.get(6);			//Ȩ�� ������ ������ 6��° ����, 1~5������ ��������
				homeOrder[homeOrder.length-1] = homeP;	//Ȩ�� Ÿ������ �Ǹ����� Ÿ���� Ȩ�� ����, ����Ÿ������ ����
				addHomeField(homeP);					//Ȩ�� ���� �ٲ� ���� �߰�
			}
			else {										//�μ��� ���� ������ ������� �������
				printLog("Pitching Substitution: "+away.bullpen.get(6)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
														//���� �����ϳ� ������� �������� �ٲ�
				awayP = away.bullpen.get(6);
				awayOrder[awayOrder.length-1] =  awayP;
				addHomeField(homeP);
			}
		}
		else if (pitcher == homeP) {					//�Է¹��� ������ �������� 100�� �ȳѰų� �̴��� 7ȸ ���ϰ� �ƴ����� ������ Ȩ�������� ���
			if (hRuns > aRuns && inning > 7 && pitcher.pitchCount > 50) {	//Ȩ�� ������ ������� �������� ����, �̴��� 8ȸ �̻��̸�, ������ �������� 50�� �Ѿ��ٸ�
				printLog("Pitching Substitution: Closer "+getCloser(home)+" for "+homeP+". "+homeP+" leaves the game with "+((int)homeP.pitchCount)+" pitches.\n");
														//������ü �α� ���, ���������� ����
				homeP = getCloser(home);				//Ȩ�� ������ �������� �ٲ�
				homeOrder[homeOrder.length-1] = homeP;	//Ȩ�� Ÿ���� �ٲ� ���� �߰�
				addHomeField(homeP);					//Ȩ�� ���� �ٲ� ���� �߰�
			}
		}
		else {											//�Է¹��� ������ �������� 100�� �ȳѰų� �̴��� 7ȸ ���ϰ� �ƴϰ� ������ Ȩ�������� �ƴ� ��� (= ����� ������ ���)
			if (aRuns > hRuns && inning > 7) {			//������� ������ Ȩ������ ���� 8�̴� �̻��� ���, ������ ����
				printLog("Pitching Substitution: Closer "+getCloser(away)+" for "+awayP+". "+awayP+" leaves the game with "+((int)awayP.pitchCount)+" pitches.\n");
														//������ü �α� ���, Ȩ���� ���� ����
				awayP = getCloser(home);
				awayOrder[awayOrder.length-1] =  awayP;
				addAwayField(awayP);
			}	
		}
		//������ �̿��� �ٸ� ���� ���� ��Ȳ�� ���� ���������� 7ȸ ���Ͽ��� ������ 100�� �̻��� ��쿡�� ��ü�ϰ� ������. ����ȭ �ʿ�
	}

	private Player getCloser(Team team) {	//���� �μ��� �Է��� ������������ ���Ϲ���
		double max = 0;
		Player closer = team.bullpen.get(0);
		for (Player p : team.bullpen) {	//���� ���� �迭�� �������� ���̺� ���� ���� ����� Ŭ������ �ø�
			if (p.SAV > max) {
				if ((team == home && p != homeP) || (team == away && p != awayP)) {
					max = p.SAV;
					closer = p;
				}
			}
		}
		return closer;		
	}

		
	private void buildFields() {		//������ ����
		for (int i = 0; i < 9; i++) {	//Ȩ��, ������� Ÿ���� ������ �߰��ϴ� for��
			addHomeField(homeOrder[i]);
			addAwayField(awayOrder[i]);
		}
		addHomeField(homeP);			//�������� �� �� ���� �߰�
		addAwayField(awayP);
	}

	private void addAwayField(Player p) {	//�μ��� ���� ������ ����� ���� �߰�
		if (p.position.equals("P"))			//������ ���
			awayField[0] = p;				//0���� ���� �Ҵ�
		else if (p.position.equals("C")) {	//������ ���
			if (awayField[1] != null)		//�����ڸ��� ������� ������
				insertToEmptySlot(awayField, awayField[1], 1, 8);
											//���� �ڸ��� ������ �߼� �ڸ��� ����
			awayField[1] = p;				//���� �ڸ��� ���� ����
		}
		//���� �� �����ǵ� ���� �˰���
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
		else if (p.position.equals("IF"))			//���߼��� ���
			insertToEmptySlot(awayField, p, 2, 5);	//���߼� �ڸ� �� �� �ƹ����� ����
		else if (p.position.equals("MI"))			//1�� ������ ���߼�
			insertToEmptySlot(awayField, p, 3, 5);	//1�� �ڸ� ������ �� �� �ƹ����� ����
		else if (p.position.equals("UT"))			//��ƿ��Ƽ, ���� ������ �� ������ ����
			insertToEmptySlot(awayField, p, 1, 8);
		else if (p.position.equals("OF"))			//�ܾ߼��� ���
			insertToEmptySlot(awayField, p, 6, 8);
	}

	private void insertToEmptySlot(Player[] field, Player p, int startIndex, int endIndex) {
		//�μ��� ���� �����迭, ����, �����ε���, ���ε��� �޾ƿ�
		for (int i = startIndex; i <= endIndex; i++) {	//�����ε������� ���ε������� for�� ����
			if (field[i] == null) {						//�� ĭ�� ������
				field[i] = p;							//�ű⿡ ���� �߰�
				break;
			}
		}
	}

	private void addHomeField(Player p) {	//�μ��� ���� ������ Ȩ ���� �߰�
		if (p.position.equals("P"))			//���� ��������� ���� �˰���
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
		for (Player p : field)//�ڹ� for���� ����:�迭�ε� foreach���� ����
			System.out.println(p);
	}

	private void printLog(String log) {
		//System.out.print(log);
		w.print(log);
	}

	private void increment_aSpot() {//Ȩ�� Ÿ�� ����
		if (aSpot == 8)
			aSpot = 0;
		else
			aSpot++;
	}
	private void increment_hSpot() {//������� Ÿ�� ����
		if (hSpot == 8)
			hSpot = 0;
		else
			hSpot++;
	}

	private boolean isOn(int result) {	//������
		if (result < 6)
			return true;
		else
			return false;
	}

	private double adjustStat(double AVG, double OVA, double LOVA) {	//���Ⱥ���
		return ((AVG*OVA)/LOVA)/((((AVG*OVA)/LOVA)+((1-AVG)*(1-OVA)/(1-LOVA))));
	}

}


