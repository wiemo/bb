/*	Player class for BaseballSim
	by Ian Zapolsky 6/4/13 */
//선수들의 스탯관련 변수들을 저장하는 클래스
import java.util.StringTokenizer;
import java.util.Scanner;
import java.io.BufferedReader;

public class Player {

	/* 	general information */
	String name, position, LR;//이름, 포지션, 좌우
	
	/* 	batting stats */	
	double G;			//출장게임수
	double PA;			//타석
	double AB;			//타수, 타석 - 볼넷 - 사구 - 희생타
	double Run;			//득점
	double Hit;			//안타
	double singles;		//단타
	double doubles;		//2루타
	double triples;		//3루타
	double HR;			//홈런
	double RBI;			//타점
	double BB;			//볼넷
	double SO;			//피삼진
	double SB;			//도루
	double CS;			//도루실패
	double GDP;			//병살타
	double HBP;			//힛바이피치
	double speedScore;	//주력
	double singleAVG;	//단타율
	double doubleAVG;	//2루타율
	double tripleAVG;	//3루타율
	double HRAVG;		//홈런타율
	double BBAVG;		//볼넷율
	double SOAVG;		//삼진율
	double HBPAVG;		//힛바이피치율
		
	/* 	piching stats (if applicaple) */
	double W;			//승
	double L;			//패
	double BF;			//상대 타자수
	double IP;			//이닝
	double pHit;		//피안타
	double pHR;			//피홈런
	double pBB;			//피볼넷
	double pSO;			//삼진
	double SAV;			//세이브
	double pG;			//출장게임수
	double GS;			//선발횟수
	double pHBP;		//사구횟수
	double pSingleAVG;	//피단타율
	double pDoubleAVG;	//피2루타율
	double pTripleAVG;	//피3루타율
	double pHRAVG;		//피홈런율
	double pBBAVG;		//피볼넷율
	double pSOAVG;		//삼진율
	double pHBPAVG;		//피사구율
	
	/* 	fielding stats */
	double PO;			//putout, 자살, 야수가 직접 아웃시킨 경우
	double ASS;			//ass, 보살, 아웃시키도록 도운 경우
	double ERR;			//에러
	double fldPCT;		//파크팩터

	/* 	game stats */
	double ab;			//타수
	double r;			//점수
	double hit;			//안타
	double rbi;			//타점
	double e;			//야수에러
	double ip;			//투구이닝
	double h;			//투수 피안타
	double er;			//투수자책점
	double bb;			//볼넷
	double so;			//삼진
	double hbp;			//힛바이피치
	double to;			//총아웃
	double pitchCount;	//총투구수

	public Player() { }	//생성자


	public void setAvg() {
		fldPCT = ((PO+ASS)/(PO+ASS+ERR));	//파크팩터 계산,	(자살+보살)/(자살+보살+에러)
		if (PA > 0) {						//타석에 한 번이라도 들어섰으면
			setSpeedScore();				//주력설정
			SOAVG = SO/PA;					//삼진율 설정, 삼진/타석
			BBAVG = BB/PA;					//볼넷율 설정, 볼넷/타석
			HBPAVG = HBP/PA;				//사구율 설정, 사구/타석
			singleAVG = (Hit - (doubles + triples + HR))/PA;	//단타율, (안타 - (2루타 + 3루타 + 홈런))/타석
			doubleAVG = doubles/PA;			//2루타율, 2루타/타석
			tripleAVG = triples/PA;			//3루타율, 3루타/타석
			HRAVG = HR/PA;					//홈런율, 홈런/타석
		}
		if (position.equals("P")) {			//포지션이 투수일 경우
        	pDoubleAVG = (pHit*.174)/BF;	//2루타율, (피안타*.174)/상대타자수
        	pTripleAVG = (pHit*.024)/BF;	//3루타율, (피안타*.24)/상대타자수
        	pHRAVG = pHR/BF;				//피홈런율, 피홈런/상대타자수
        	pSingleAVG = (pHit/BF) - pDoubleAVG - pTripleAVG - pHRAVG;	//피단타율, (피안타/상대타자수) - 피2루타율 - 피3루타율 - 피홈런율
        	pBBAVG = pBB/BF;				//볼넷율, 볼넷/상대타자수
			pSOAVG = pSO/BF;				//삼진율, 삼진/상대타자수
			pHBPAVG = pHBP/BF;				//사구율, 사구/상대타자수
		}
    }		

	public boolean isFast() {				//주력이 빠른지 느린지
		if (speedScore > 5)					//5이상이면 빠름
			return true;
		else
			return false;
	}

	public void clearGameStats() {			//모든 스탯을 초기화시킴
		pitchCount = ab = r = hit = rbi = e = ip = h = er = bb = hbp = to = so = 0;
	}

	private void setSpeedScore() {						//주력설정
		double f1 = (SB + 3)/(SB+CS+7);					//요소1은 (도루+3)/(도루+도루실패+7)
		f1 = (f1 - .4)*20;								//보정
		f1 = checkFactor(f1);							//값 체크

		double f2 = (SB + CS)/(singles+BB+HBP);			//요소2는 (도루+도루실패)/(단타+볼넷+사구)
		f2 = (Math.sqrt(f2)/.07);						//값 보정
		f2 = checkFactor(f2);
	
		double f3 = triples/(AB-HR-SO);					//요소3은 3루타/(타수-홈런-삼진)
		f3 = f3/.0016;
		f3 = checkFactor(f3);

		double f4 = (Run - HR) / (Hit + BB + HBP - HR);	//(득점-홈런)/(안타+볼넷+사구-홈런)
		f4 = (f4 - .1)*25;
		f4 = checkFactor(f4);

		double f5 = GDP/(AB - HR - SO);					//병살타/(타수-홈런-삼진)
		f5 = (.063 - f5)/.007;
		f5 = checkFactor(f5);
	
		double f6 = getF6();
		f6 = checkFactor(f6);
		
		speedScore = ((f1+f2+f3+f4+f5+f6)/6);
	}

	private double getF6() {	//포지션별 요소
		if (position.equals("P"))			//투수일 경우
			return 0;
		else if (position.equals("C"))		//포수일 경우
			return 1;
		else if (position.equals("1B"))		//1루수일 경우
			return 2;
		else if (position.equals("2B"))		//2루수일 경우
			return (((PO + ASS)/G)/4.8)*6;	// (((자살+보살)/게임)/4.8)*6
		else if (position.equals("3B"))		//3루수일 경우
			return (((PO + ASS)/G)/2.65)*4;	// (((자살+보살)/게임)/2.65)*4
		else if (position.equals("SS"))		//유격수일 경우
			return (((PO + ASS)/G)/4.6)*7;	// (((자살+보살)/게임)/4.6)*7
		else								//외야수일 경우
			return (((PO + ASS)/G)/2.0)*6;	// (((자살+보살)/게임)/2.0)*6
	}

	private double checkFactor(double factor) {	//팩터 보정
		if (factor > 10)		//값이 10보다 크면 10으로
			return 10;
		else if (factor < 0)	//값이 0보다 작으면 0으로
			return 0;
		else					//아닐경우 들어온 값 그대로 반환
			return factor;
	}


	public String toString() {
		return name+" ("+position+")";
	} 
}	
