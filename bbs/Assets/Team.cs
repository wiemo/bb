using UnityEngine;
using System.Collections.Generic;

public class Team : MonoBehaviour {

    string name;                //팀이름
    int year;                   //년도
    public List<Player> roster;
    public List<Player> bullpen;
    
    public Team()
    {               //팀 클래스 생성자
        roster = new List<Player>();   //팀 로스터 초기화
        bullpen = new List<Player>();  //팀 불펜 초기화
    }

    // 문자열을 하나 받아와서 년도, 이름을 잘라내서 대입했는데 바꿈
    public void setInfo(string info)
    {
        year = int.Parse(info.Substring(4, 4));
        name = info.Substring(9, info.Length - 9);
    }
    

    public void setInfo(string y, string n)
    {
        year = int.Parse(y);
        name = n;
    }

    public void addPlayer(Player input)
    {   //선수추가 함수, 인수로 선수넣음
        roster.Add(input);                  //로스터에 선수 추가함
        if (input.position.Equals("P"))     //만약 투수면 불펜에도 추가
            bullpen.Add(input);
    }

    public void zeroOut()
    {   //로스터에 있는 모든 선수들 게임스탯 초기화
        foreach (Player p in roster)
        {
            p.clearGameStats();
        }
    }

    public Player getPlayer(string name)
    {   //인수로 받은 이름의 선수를 리턴하는 함수
        Player target = roster[0];
        foreach(Player p in roster)
        {
            if (p.name.Equals(name))
                target = p;
        }
        return target;
    }

    public void setAverages()
    {   //선수 스탯 평균계산

        foreach (Player p in roster)
            p.SetAVG();
    }

    public string printRoster()
    {   //로스터 내의 선수들을 출력함
        string result = "" + year + " " + name + "\n";
        result += "ROSTER:\n";
        foreach (Player p in roster)
            result += p + " (" + p.position + ")\n";
        return result;
    }
}
