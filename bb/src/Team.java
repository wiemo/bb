/*	Team class for BaseballSim
	by Ian Zapolsky 6/4/13 */

import java.util.ArrayList;

public class Team {

	String name;				//팀이름
	int year;					//년도
	ArrayList<Player> roster;	//팀 로스터, 플레이어 배열리스트
	ArrayList<Player> bullpen;	//팀 불펜, 플레이어 배열리스트
		
	public Team() {				//팀 클래스 생성자
		roster = new ArrayList<Player>();	//팀 로스터 초기화
		bullpen = new ArrayList<Player>();	//팀 불펜 초기화
	}

	public void setInfo(String info) {
		year = Integer.valueOf(info.substring(4,8));
		name = info.substring(9,(info.length()-5));
	}

	public void addPlayer(Player input) {	//선수추가 함수, 인수로 선수넣음
		roster.add(input);					//로스터에 선수 추가함
		if (input.position.equals("P"))		//만약 투수면 불펜에도 추가
			bullpen.add(input);
	}

	public void zeroOut() {					//로스터에 있는 모든 선수들 게임스탯 초기화
		for (Player p : roster)
			p.clearGameStats();
	}

	public Player getPlayer(String name) {	//인수로 받은 이름의 선수를 리턴하는 함수
		Player target = roster.get(0);
		for (Player p : roster) {
			if (p.name.equals(name))
				target = p;
		}
		return target;
	}

	public void setAverages() {				//선수 스탯 평균계산
		for (Player p : roster)
			p.setAvg();
	}

	public String printRoster() {			//로스터 내의 선수들을 출력함
		String result = ""+year+" "+name+"\n";
		result += "ROSTER:\n";
		for (Player i : roster)
			result += i+" ("+i.position+")\n";
		return result;
	}

	public String toString() {
		return ""+name+" ("+year+")";
	}
}
