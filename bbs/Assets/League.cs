using UnityEngine;
using System.Collections;

public class League : MonoBehaviour {

    //리그 수치 스탯
    internal int year;                           //년도
    internal double BF;                          //상대타자수
    internal double Hit;                         //안타
    internal double singles;                     //단타
    internal double doubles;                     //2루타
    internal double triples;                     //3루타
    internal double HR;                          //홈런
    internal double BB;                          //볼넷
    internal double SO;                          //삼진
    internal double HBP;                         //사구

    //리그평균스탯
    internal double leagueOVASingle;             //리그평균단타율
    internal double leagueOVADouble;
    internal double leagueOVATriple;
    internal double leagueOVAHR;
    internal double leagueOVABB;
    internal double leagueOVASO;
    internal double leagueOVAHBP;

    public League() { }

    public void setLeagueOVA()
    {
        leagueOVASingle = singles / BF; //리그 평균 단타율 = 단타/상대타자
        leagueOVADouble = doubles / BF; //리그 평균 2루타율 = 2루타 / 상대타자
        leagueOVATriple = triples / BF;
        leagueOVAHR = HR / BF;
        leagueOVABB = BB / BF;
        leagueOVASO = SO / BF;
        leagueOVAHBP = HBP / BF;
    }

    public void setYear(int init_year)
    {   //인수로 받아온 연도를 연도에 저장
        year = init_year;
    }
}
