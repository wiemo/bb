/* 	League class for BaseballSim
	by Ian Zapolsky 6/6/13 */

import java.util.ArrayList;
import java.util.Scanner;

public class League {

	//리그 수치 스탯
	int year;							//년도
	double BF;							//상대타자수
	double Hit;							//안타
	double singles;						//단타
	double doubles;						//2루타
	double triples;						//3루타
	double HR;							//홈런
	double BB; 							//볼넷
	double SO;							//삼진
	double HBP;							//사구
	
	//리그평균스탯
	double leagueOVASingle;				//리그평균단타율
	double leagueOVADouble; 
	double leagueOVATriple;
	double leagueOVAHR;
	double leagueOVABB; 
	double leagueOVASO;
	double leagueOVAHBP;

	public League() { }

	public void setLeagueOVA() {
		leagueOVASingle = singles/BF;	//리그 평균 단타율 = 단타/상대타자
		leagueOVADouble = doubles/BF;	//리그 평균 2루타율 = 2루타 / 상대타자
		leagueOVATriple = triples/BF;
		leagueOVAHR = HR/BF;
		leagueOVABB = BB/BF;
		leagueOVASO = SO/BF;
		leagueOVAHBP = HBP/BF;
	}

	public void setYear(int init_year) {	//인수로 받아온 연도를 연도에 저장
		year = init_year;
	}

	public String toString() {
		return String.valueOf(year);
	}

}
