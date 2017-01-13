using UnityEngine;
using System.Collections;

public class Player : MonoBehaviour {


    /*  General Infomation  */
    internal string name;        //이름
    internal string position;    //
    internal string LR;


    /*  Batting Stats   */
    internal double G;           //출장게임수
    internal double PA;          //타석
    internal double AB;          //타수, 타석 - 볼넷 - 사구 - 희생타
    internal double Run;         //득점
    internal double Hit;         //안타
    internal double singles;     //단타
    internal double doubles;     //2루타
    internal double triples;     //3루타
    internal double HR;          //홈런
    internal double RBI;         //타점
    internal double BB;          //볼넷
    internal double SO;          //피삼진
    internal double SB;          //도루
    internal double CS;          //도루실패
    internal double GDP;         //병살타
    internal double HBP;         //힛바이피치
    internal double speedScore;  //주력
    internal double singleAVG;   //단타율
    internal double doubleAVG;   //2루타율
    internal double tripleAVG;   //3루타율
    internal double HRAVG;       //홈런타율
    internal double BBAVG;       //볼넷율
    internal double SOAVG;       //삼진율
    internal double HBPAVG;		 //힛바이피치율


    /*  Pitching Stats  */
    internal double W;           //승
    internal double L;           //패
    internal double BF;          //상대 타자수
    internal double IP;          //이닝
    internal double pHit;        //피안타
    internal double pHR;         //피홈런
    internal double pBB;         //피볼넷
    internal double pSO;         //삼진
    internal double SAV;         //세이브
    internal double pG;          //출장게임수
    internal double GS;          //선발횟수
    internal double pHBP;        //사구횟수
    internal double pSingleAVG;  //피단타율
    internal double pDoubleAVG;  //피2루타율
    internal double pTripleAVG;  //피3루타율
    internal double pHRAVG;      //피홈런율
    internal double pBBAVG;      //피볼넷율
    internal double pSOAVG;      //삼진율
    internal double pHBPAVG;     //피사구율


    /* 	fielding stats */
    internal double PO;          //putout, 자살, 야수가 직접 아웃시킨 경우
    internal double ASS;         //ass, 보살, 아웃시키도록 도운 경우
    internal double ERR;         //에러
    internal double fldPCT { get; set; }      //파크팩터


    /* 	game stats */
    internal double ab { get; set; }          //타수
    internal double r { get; set; }           //점수
    internal double hit { get; set; }         //안타
    internal double rbi { get; set; }         //타점
    internal double e { get; set; }           //야수에러
    internal double ip { get; set; }          //투구이닝
    internal double h { get; set; }           //투수 피안타
    internal double er { get; set; }          //투수자책점
    internal double bb { get; set; }          //볼넷
    internal double so { get; set; }          //삼진
    internal double hbp { get; set; }         //힛바이피치
    internal double to { get; set; }          //총아웃
    internal double pitchCount { get; set; }  //총투구수

    /*  Private 메소드   */

    /// <summary>
    /// 팩터 수치를 보정
    /// </summary>
    /// <param name="factor">보정할 팩터 수치</param>
    /// <returns>보정된 값</returns>
    private double checkFactor(double factor)
    {
        if (factor > 10)        //값이 10보다 크면 10으로
            return 10;
        else if (factor < 0)    //값이 0보다 작으면 0으로
            return 0;
        else                    //아닐경우 들어온 값 그대로 반환
            return factor;
    }

    /// <summary>
    /// 주력 설정 메소드에서 사용하는 6번째 팩터를 구하는 메소드
    /// 포지션 별로의 팩터를 반환함
    /// </summary>
    /// <returns>각 포지션마다의 팩터값</returns>
    private double getF6()
    {
        if (position.Equals("P"))           //투수일 경우
            return 0;
        else if (position.Equals("C"))      //포수일 경우
            return 1;
        else if (position.Equals("1B"))     //1루수일 경우
            return 2;
        else if (position.Equals("2B"))     //2루수일 경우
            return (((PO + ASS) / G) / 4.8) * 6;    // (((자살+보살)/게임)/4.8)*6
        else if (position.Equals("3B"))     //3루수일 경우
            return (((PO + ASS) / G) / 2.65) * 4;   // (((자살+보살)/게임)/2.65)*4
        else if (position.Equals("SS"))     //유격수일 경우
            return (((PO + ASS) / G) / 4.6) * 7;    // (((자살+보살)/게임)/4.6)*7
        else                                //외야수일 경우
            return (((PO + ASS) / G) / 2.0) * 6;	// (((자살+보살)/게임)/2.0)*6
    }

    /// <summary>
    /// 이 선수의 주력 설정
    /// </summary>
    private void SetSpeedScore()
    {
        double f1 = (SB + 3) / (SB + CS + 7);                   //요소1은 (도루+3)/(도루+도루실패+7)
        f1 = (f1 - .4) * 20;                                //보정
        f1 = checkFactor(f1);                           //값 체크
        
        double f2 = (SB + CS) / (singles + BB + HBP);           //요소2는 (도루+도루실패)/(단타+볼넷+사구)
        f2 = (Mathf.Sqrt((float)f2) / .07);                     //값 보정
        f2 = checkFactor(f2);

        double f3 = triples / (AB - HR - SO);                   //요소3은 3루타/(타수-홈런-삼진)
        f3 = f3 / .0016;
        f3 = checkFactor(f3);

        double f4 = (Run - HR) / (Hit + BB + HBP - HR); //(득점-홈런)/(안타+볼넷+사구-홈런)
        f4 = (f4 - .1) * 25;
        f4 = checkFactor(f4);

        double f5 = GDP / (AB - HR - SO);                   //병살타/(타수-홈런-삼진)
        f5 = (.063 - f5) / .007;
        f5 = checkFactor(f5);

        double f6 = getF6();
        f6 = checkFactor(f6);

        speedScore = ((f1 + f2 + f3 + f4 + f5 + f6) / 6);
    }


    /*  Public 메소드  */

    /// <summary>
    /// 이 선수의 평균 스탯을 계산
    /// </summary>
    public void SetAVG()
    {
        fldPCT = ((PO + ASS) / (PO + ASS + ERR));
        if(PA>0)                                //타석에 한 번이라도 들어섰으면
        {
            SetSpeedScore();                    //주력설정
            SOAVG = SO / PA;                    //삼진율 설정, 삼진/타석
            BBAVG = BB / PA;                    //볼넷율 설정, 볼넷/타석
            HBPAVG = HBP / PA;                  //사구율 설정, 사구/타석
            singleAVG = (Hit - (doubles + triples + HR)) / PA;  //단타율, (안타 - (2루타 + 3루타 + 홈런))/타석
            doubleAVG = doubles / PA;           //2루타율, 2루타/타석
            tripleAVG = triples / PA;           //3루타율, 3루타/타석
            HRAVG = HR / PA;					//홈런율, 홈런/타석
        }
        if (position.Equals("P"))               //포지션이 투수일 경우
        {
            pDoubleAVG = (pHit * .174) / BF;    //2루타율, (피안타*.174)/상대타자수
            pTripleAVG = (pHit * .024) / BF;    //3루타율, (피안타*.24)/상대타자수
            pHRAVG = pHR / BF;                  //피홈런율, 피홈런/상대타자수
            pSingleAVG = (pHit / BF) - pDoubleAVG - pTripleAVG - pHRAVG;    //피단타율, (피안타/상대타자수) - 피2루타율 - 피3루타율 - 피홈런율
            pBBAVG = pBB / BF;                  //볼넷율, 볼넷/상대타자수
            pSOAVG = pSO / BF;                  //삼진율, 삼진/상대타자수
            pHBPAVG = pHBP / BF;                //사구율, 사구/상대타자수
        }
    }

    /// <summary>
    /// 이 선수의 주력이 빠른지 느린지 반환
    /// </summary>
    /// <returns>주력이 빠른지, 느린지</returns>
    public bool isFast()                        
    {               
        if (speedScore > 5)                     //5이상이면 빠름
            return true;
        else
            return false;
    }

    /// <summary>
    /// 모든 스탯을 초기화시킴
    /// </summary>
    public void clearGameStats()
    {
        pitchCount = ab = r = hit = rbi = e = ip = h = er = bb = hbp = to = so = 0;
    }
}
